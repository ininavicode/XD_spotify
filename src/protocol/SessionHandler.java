package protocol;

import search_engine.*;
import song.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

// ############################ SESSION HANDLER CLASS ############################
/**
 * This class will handle the different sessions of the various users that can access the server to request list of songs,
 * as well as the searches needed.
 * Inner class of server, once a server is created, a session handler will need to be created too.
 */
public class SessionHandler {     
    // ############ CONSTANTS ############
    /**
     * The frequency a signal to decrease the time to leave of each historial will be generated.
     */
    private static final short FREQ_OF_TIMEOUT = 1000; 
    
    /**
     * The default time to live of a historial list.
     */
    private static final short DEFAULT_TTL = 100;
    /*
        * DISCLAIMER: To obtain the time a list remains on the server,
        *      time = DEFAULT_TTL / (FREQ_OF_TIMEOUT/1000) (s) 
        */

    // ############ INSTANCE ATTRIBUTES ############
    private SearchEngine searchEngine;
    /**
     * HashMap to control the users, short = cookie, String = last list.
     */
    private HashMap<Long, HistorialInfo> historialOfSearches; 

    /**
     * Object to control the synchronisation throghout the app.
     */
    private Object syncroHandler;

    /**
     * Thread that will calculate the timeout of the lists.
     */
    private Thread timeoutCalc;

    /**
     * Constructor of the class.
     * 
     * @param fileName The fileName from which the server will have the list of all of the songs available.
     */
    public SessionHandler(String fileName) {
        searchEngine = new SearchEngine(fileName);
        syncroHandler = new Object(); // To control the timeouts to the different threads.
        historialOfSearches = new HashMap<>(100); // Initial capacity to avoid constantly making O(n) arrangements of the hash.
        // Generation and start of the thread that will count the timeouts.
        timeoutCalc = new Thread(() -> {
            timeoutControl();
        });
        timeoutCalc.start();
    }

    /**
     * Method to add a song to the search engine.
     * 
     * @param song The song to add.
     * @param mp3dir The directory of the song.
     */
    public void addSong(Song song, String mp3dir) {
        searchEngine.addSong(song, mp3dir);
    }

    // For now, not permitted.
    // /**
    //  * Method to remove a song from the search engine.
    //  * 
    //  * @param song The song to be removed.
    //  */
    // public void removeSong(Song song) {
    //     searchEngine.removeSong(song);
    // }

    /**
     * Method that makes a search in a historial list of a user or the total list of songs.
     * 
     * @param cookie The cookie that identifies the session.
     * @param cond The string that identifies the condition.
     * @return The list of songs that match the condition specified in string format to be sent to the user + the cookie (it 
     *         could be the same or a new generated if the session expired).
     */
    public Protocol.ResponseSearchEngine_t makeSearch(long cookie, String cond) {
        Protocol.ResponseSearchEngine_t result = new Protocol.ResponseSearchEngine_t(); // The instance that will be the result of the search.
        synchronized(syncroHandler) {
            if(historialOfSearches.containsKey(cookie)) {
                result.cookie = cookie; // The cookie is still active, so there is no need in generating a new one.
                HistorialInfo tempHist = historialOfSearches.get(cookie);
                tempHist.ttl = DEFAULT_TTL; // Restart ttl, new query for this user done.
                String lastSearch = tempHist.lastSearch;
                if(cond.length() != 0) {
                    tempHist.lastSearch = cond; // Subtitute the last search done in the session.
                    if(!lastSearch.startsWith(cond.toLowerCase()) || (lastSearch.length() > cond.length())) { // If the condition contains less characters or is less than the initial condition ...
                        result.songList = searchEngine.getSongsWithCondition(cond); // ... the search will be done in the total list of songs.    
                    }
                    else {
                        result.songList = SearchEngine.getSongsWithCondition(SongList.fromByteRaw(tempHist.histList.getBytes()), cond);
                    }
                    tempHist.histList = SongList.toString(result.songList);
                }
                else { // A void search means that the session has expired.
                    historialOfSearches.remove(cookie);
                    result.songList = new ArrayList<>(); // An empty list, as no coincidence has been made.
                    result.cookie = -1; // Automatically end the session.
                }
            }
            else { // The session did not exist or had expired for a timeout, new search needed.
                if(cond.length() != 0) {
                    result.songList = searchEngine.getSongsWithCondition(cond);    
                    result.cookie = allocateUser(cond, SongList.toString(result.songList), false);  // allocate the session in the hash map.
                }
                else {
                    result.songList = new ArrayList<>(); // An empty list, as no coincidence has been made.
                    result.cookie = -1; // Automatically end the session.
                }
            }
        }
        return result; 
    }
    /**
     * Method to close an active session by brutal force, without a timeout.
     * 
     * @param cookie The cookie of the session to be closed.
     */
    public void closeSession(long cookie) {
        if(historialOfSearches.containsKey(cookie))
            historialOfSearches.remove(cookie);
        // if not, no removal made.
    }

    /**
     * Method that generates a user with no ttl in order to transmit correctly a song.
     * 
     * @param cookie The cookie of the actual session.
     * @param songName The song name of the song requested.
     * @return
     */
    public long generateInfiniteUser(long cookie, String songName) {
        long newCookie;
        synchronized(syncroHandler) {
            if(historialOfSearches.containsKey(cookie)) {
                HistorialInfo aux = historialOfSearches.get(cookie);
                aux.ttl = -1; // Modify the session to become permanent.
                aux.histList = songName;
                aux.lastSearch = songName; // Both variables hold the song file name in the context of the server.
                newCookie = cookie; // Session has not expired.
            }
            else {
                // Generate new session with no timeout.
                newCookie = allocateUser(songName, songName, true); // In this type of request, both the condition and the list hold the actual name of the song.
            }
        }
        return newCookie;
    }

    public String retreiveSongMP3Name(long cookie) {
        String result = null;
        synchronized(syncroHandler) {
            if(historialOfSearches.containsKey(cookie)) {
                result = historialOfSearches.get(cookie).lastSearch; // both last search and histlist contain the name of the mp3 file.
            }
        }
        return result;
    }

    // ##################### PRIVATE METHODS/CLASSES #####################
    /**
     * Method that allocates a user in the hashmap of the historial lists.
     * 
     * @param cond The first search of the user.
     * @param listOfSongs The list of songs generated by the search.
     * @param infiniteTimeout True if this session should not be ended, false otherwise.
     * @return The cookie that identifies the session created for the user.
     * @pre The user must not exist in the list of searches.
     */
    private long allocateUser(String cond, String listOfSongs, boolean infiniteTimeout) {
        HistorialInfo histInfo = new HistorialInfo();
        Date actualDate = new Date();
        long cookie = generateCookie(actualDate, listOfSongs);
        histInfo.ttl = infiniteTimeout ? (short)-1 : DEFAULT_TTL;
        histInfo.histList = listOfSongs;
        histInfo.lastSearch = cond.toLowerCase();
        synchronized(syncroHandler) {
            historialOfSearches.put(cookie, histInfo);
        }
        return cookie;
    }

    /**
     * Method that synchronisedly controls the time to live for each historial list in the server.
     * The thread sleeps for FREQ_OF_TIMEOUT miliseconds, and then decrements by 1 unit the time to live
     * of each element of the hash. If it is equal or inferior to 0, the list will be removed from the historial hash.
     */
    private void timeoutControl() {
        while(true) { // Infinite loop.
            try {
                Thread.sleep(FREQ_OF_TIMEOUT); // Time the thread will be put to asleep.
            } 
            catch(InterruptedException exc) {
                // Non expected exception, as the thread itself is the only one that will control its flow.
            }
            /*
                * As the keys and values cannot be accessed inside of the forEach method, firstly the keys will be stored 
                * in an ArrayList. Then, this list will be inspected, and for each element inside of this list, the element will
                * be removed from the list of historic searches.
                */
            synchronized(syncroHandler) {
                ArrayList<Long> tempList = new ArrayList<>(); // To store the keys which will be eliminated.
                historialOfSearches.forEach((key, value) -> {
                    if(value.ttl != -1 && --value.ttl <= 0) { // if value.ttl == -1, permanent session.
                        tempList.add(key);
                    }
                });
                for(Long elem : tempList) {
                    historialOfSearches.remove(elem);
                }
            }
        }
    }

    /**
     * Generator of a cookie from a timestamp + a list of songs in a string.
     * 
     * @param date The timestamp needed.
     * @param listOfSongs The list of songs in string format.
     * @return The cookie generated.
     */
    private static long generateCookie(Date date, String listOfSongs) {
        return ((date.hashCode() << 32) | listOfSongs.hashCode());
    }

    /**
     * This class will act as a struct of the String which contains the historial list + the time to live of each list.
     * Private as it will not be accessed from the outside.
     */
    private class HistorialInfo {
        private String histList;
        private String lastSearch;
        private short ttl;
    }
}
