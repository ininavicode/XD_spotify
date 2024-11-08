package search_engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import song.*;

/**
 * Class that defines the search engine for the server and the client.
 * It has a hash for songs and an arraylist for saving the info of the songs.
 */

public class SearchEngine {
    // ##################### static properties #####################
    // sugerences paremeters
    private static final int DIV_FACTOR_DISTANCE_OF_CONTAINS = 2;
    private static final int N_SUGERENCES = 5;

    // Instance attributes
    private HashMap<String, Song> songsNames; // list that contains the songs in string format to make the faster
    String fileName;

    /**
     * Constructor of the class.
     * 
     * @param fileName The file from which the search engine will initially load all the data.
     * @pre The filename must be in the correct format, with the list of songs correctly formatted.
     */
    public SearchEngine(String fileName) {
        songsNames = new HashMap<>();
        
        try(Scanner fileReader = new Scanner(new File(fileName))) {
            Song nextSong;
            while(fileReader.hasNextLine()) {
                nextSong = new Song(fileReader.nextLine());
                songsNames.put(nextSong.toFilename(), nextSong);
            }
            this.fileName = fileName;
        }
        catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Getter of a song by its name.
     * 
     * @param song The name of the song.
     * @return Return the MP3 name of the song, null if it is not listed.
     */
    public String getMP3ByName(Song song) {
        if (songsNames.containsValue(song) == false) {
            return null;
        }
        else {
            return song.toFilename();
        }

    }

    /**
     * Returns a copy of the list of songs.
     * 
     * @return List of the songs.
     */
    public ArrayList<Song> getSongsList() {
        return new ArrayList<>(songsNames.values());
    }

    /**
     * Indicates if the hash contains the mp3.
     * 
     * @param mp3Name The MP3 name to be checked.
     * @return True if it is contained, false otherwise.
     */
    public boolean containsMP3(String mp3Name) {

        return songsNames.containsKey(mp3Name);
    }

    /**
     * Returns all the songs that have a coincidence with cond.
     * 
     * @param cond The string that conditions the result.
     * @return The sublist of songs that contain cond. If none matches cond, an empty list.
     * @pre cond != null.
     */
    public ArrayList<Song> getSongsWithCondition(String cond) {
        ArrayList<Song> newList = new ArrayList<>(N_SUGERENCES);
        String[] names = songsNames.keySet().toArray(new String[0]);

        ArrayList<Integer> indexResults = indexSugerencesOf(names, cond);

        for (Integer intg : indexResults) {
            // this gets a song by its key
            newList.add(songsNames.get(names[intg]));
        }

        return newList;
    }

    // TODO: EDIT FILE WITH NEW SONG ADDITIONS.
    /**
     * Adds a song to the list and the hash of songs.
     * 
     * @param song The song which will be added.
     * @param mp3dir The name of the mp3.
     */
    public void addSong(Song song, String mp3dir) {
        if(song != null && mp3dir != null) {
            songsNames.put(mp3dir, song);
            try(FileWriter writer = new FileWriter(new File(fileName), true)) {
                writer.write(song.toCharRaw() + '\n'); // Append the new song to the end of the file.
            }
            catch (IOException exc) {
                songsNames.remove(mp3dir);
                exc.printStackTrace();
            }
        }
    }

    // For now this option is not available.
    // public void removeSong(Song song) {
    //     if(song != null) songsHash.remove(song);
    // }

    // ##################### CLASS METHODS #####################
    /**
     * Class method that allows to search in a subList for the coincidences to cond.
     * 
     * @param subList The sublist from which the search will be executed.
     * @param cond The string that holds the caracter 
     * @return The sublist of songs that start with cond. If none matches cond, an empty list.
     * @pre subList != null && cond != null.
     */
    public static ArrayList<Song> getSongsWithCondition(ArrayList<Song> subList, String cond) {
        ArrayList<Song> newList = new ArrayList<>(N_SUGERENCES);

        // convert the song list to list of filenames
        String[] names = new String[subList.size()];
        for (int i = 0; i < subList.size(); i++) {
            names[i] = subList.get(i).toFilename();
        }

        ArrayList<Integer> indexResults = indexSugerencesOf(names, cond);

        for (Integer intg : indexResults) {
            // this gets a song by its key
            newList.add(subList.get(intg));
        }

        return newList;
    }

    // ##################### static methods #####################
    public static ArrayList<Integer> indexSugerencesOf(String[] list, String sch) {
        sch = sch.toLowerCase();

        int distanceOfStrings[] = new int[list.length]; // here is stored the acumulated distance for each string of the list
        
        final int listLen = list.length;
        final int nSugerences = Math.min(listLen, N_SUGERENCES);

        int maxDistances[] = new int[nSugerences]; // 5 sugerences
        ArrayList<Integer> indexMaxDistances = new ArrayList<>(nSugerences);
        
        // init distances
        for (int i = 0; i < nSugerences; i++) {
            maxDistances[i] = -9999999;
            indexMaxDistances.add(-1);
        }

        // add other distance calculations and find the max distances
        for (int j = 0; j < listLen; j++) {

            String actualStr = list[j].toLowerCase();
            distanceOfStrings[j] = specialContains(sch, actualStr);

            if (actualStr.startsWith(sch)) {

                distanceOfStrings[j] = 3 * distanceOfStrings[j];   
            }

            // save the max distance value found
            for (int i = 0; i < nSugerences; i++) {
                if (distanceOfStrings[j] > maxDistances[i]) {
                    maxDistances[i] = distanceOfStrings[j];
                    indexMaxDistances.remove(indexMaxDistances.size() - 1);
                    indexMaxDistances.add(i, j);
                    
                    break;
                }
            }
        }

        // exclude so low distance sugerences
        // low distance is considered less than a 75% of the top sureged string
        int excludeThreshold = 3 * maxDistances[0] / 4; // max distance * (3/4)

        for (int i = indexMaxDistances.size() - 1; i >= 0; i--) {
            if (maxDistances[i] >= excludeThreshold) {
                break;
            }
            else {
                indexMaxDistances.remove(i);
            }
        }

        return indexMaxDistances;
    }


    /**
     * 
     * @return Performs a contains method with all the substrings possible of "seq"
     * of continous characters
     * @example seq = "pat"
     * len = 1
     *  contains(p)
     *  contains(a)
     *  contains(t)
     * len = 2
     *  contains(pa)
     *  contains(at)
     * len = 3
     *  contains(pat)
     */
    private static int specialContains(String seq, String str) {
        final int seqLen = seq.length();

        int acumulatedValue = 0;

        for (int len = 1; len <= seqLen; len++) {
            final int totalIterations = seqLen - len + 1;

            for (int i = 0; i < totalIterations; i++) {
                if (str.contains(seq.substring(i, i + len))) {
                    acumulatedValue += len * len;
                }
            }
        }

        return acumulatedValue;
    }
}
