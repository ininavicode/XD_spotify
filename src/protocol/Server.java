package protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import packetizer.Packetizer;
import search_engine.SearchEngine;
import song.*;



public class Server {
    private int serverPort;
    private DatagramSocket datagramSocket;

    private DatagramPacket datagramPacket;
    private final byte[] datagramPacketBuffer;

    private InetAddress lastPacketAddress;
    private int lastPacketPort;
    private Protocol.COMMAND_TYPE lastPacketCommandType;
    private byte[] lastPacketData;
    private ByteBuffer lastPacketByteBuffer; // keeps the data of the last received packet withour the command type first byte

    // ##################### constructors #####################
    public Server(int serverPort) throws IOException {
        this.serverPort = serverPort;
        datagramPacketBuffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        datagramPacket = new DatagramPacket(datagramPacketBuffer, (int)Protocol.DATAGRAM_MAX_SIZE);

        datagramSocket = new DatagramSocket(this.serverPort);
    }

    // ##################### methods #####################
    public void waitForPacket() throws IOException {
        datagramSocket.receive(datagramPacket);

        lastPacketByteBuffer = ByteBuffer.allocate(datagramPacket.getLength());
        lastPacketByteBuffer.put(datagramPacket.getData(), 0, datagramPacket.getLength());

        // parse source of the packet
        lastPacketAddress = datagramPacket.getAddress();
        lastPacketPort = datagramPacket.getPort();

        // save the command type
        lastPacketCommandType = Protocol.COMMAND_TYPE.fromByte(lastPacketByteBuffer.array()[0]);

        // save the data of the packet
        // the first byte of a request is allways the command type, so the data is from the byte 1 to the end
        lastPacketData = new byte[lastPacketByteBuffer.array().length - 1];
        lastPacketByteBuffer.get(1, lastPacketData);
        
        // save the data to post process it without the command type 
        lastPacketByteBuffer.put(0, datagramPacket.getData(), 1, datagramPacket.getLength() - 1);

    }


    // ##################### getters #####################
    public Protocol.COMMAND_TYPE getLastPacketCommandType() {
        return lastPacketCommandType;

    }

    /**
     * @return A byte[] copy, containing the data of the last packet received.
     */
    public byte[] getLastPacketData() {
        return lastPacketData.clone();
    }

    /**
     * Gets automatically the song of the name of the last packet, in function of the last
     *  command type
     */
    public String getLastPacket_SongName() {
        byte[] encodedStr;

        // ##################### SONG_MP3_N_PACKETS_REQUEST #####################
        if (lastPacketCommandType == Protocol.COMMAND_TYPE.SONG_MP3_N_PACKETS_REQUEST) {
            return new String(lastPacketData);

        }

        // ############ SEARCH_ENGINE_REQUEST ############
        
        else if (lastPacketCommandType == Protocol.COMMAND_TYPE.SEARCH_ENGINE_REQUEST) {
            encodedStr = new byte[lastPacketData.length - 8]; // TODO: DOESN'T GENERATE A CORRECT STRING.
            lastPacketByteBuffer.get(8, encodedStr, 0, encodedStr.length);

            return new String(encodedStr);

        }

        // ############ SONG_MP3_PACKETS_RANGE_REQUEST ############

        else if (lastPacketCommandType == Protocol.COMMAND_TYPE.SONG_MP3_PACKETS_RANGE_REQUEST) {
            encodedStr = new byte[lastPacketData.length - 4];
            lastPacketByteBuffer.get(4, encodedStr, 0, encodedStr.length);

            return new String(encodedStr);
        }

        return null;

    }

    // ############ SEARCH_ENGINE_REQUEST ############
    /**
     * @pre Last command type must be SEARCH_ENGINE_REQUEST
     */
    public long getLastPacket_Cookie() {
        // | byte index | data 
        // | ---------- | -----------------|
        // | 0          | command type     |
        // | 1 .. 8     | cookie           |
        // | 9 .. 1500  | string of search |
        return lastPacketByteBuffer.getLong(0);
    }

    /**
     * @pre Last command type must be SONG_MP3_PACKET_REQUEST
     */
    // ############ SONG_MP3_PACKET_REQUEST ############
    public short getLastPacket_PacketID() {

        // | byte index | data         |
        // | ---------- | ------------ |
        // | 0          | command type |
        // | 1...2      | packet id    |
        // | 3...1500   | song name    |

        return lastPacketByteBuffer.getShort(0);
    }


    // ############ SONG_MP3_PACKETS_RANGE_REQUEST ############
    public short getLastPacket_StartPacketID() {

        // | byte index | data                         |
        // | ---------- | ---------------------------- |
        // | 0          | command type                 |
        // | 1...2      | packet id start (included)   |
        // | 3...4      | packet id end (not included) |
        // | 5...1500   | song name                    |

        return lastPacketByteBuffer.getShort(0);
    }

    public short getLastPacket_EndPacketID() {

        // | byte index | data                         |
        // | ---------- | ---------------------------- |
        // | 0          | command type                 |
        // | 1...2      | packet id start (included)   |
        // | 3...4      | packet id end (not included) |
        // | 5...1500   | song name                    |

        return lastPacketByteBuffer.getShort(2);
    }

    // ##################### responses #####################
    public void responseSearchEngine(Protocol.ResponseSearchEngine_t response) throws IOException {
        byte[] encodedSongList = SongList.toByteRaw(response.songList);

        ByteBuffer responseBuffer = ByteBuffer.allocate(encodedSongList.length + 8);    // data + cookie

        responseBuffer.putLong(response.cookie);    // add the cookie.
        responseBuffer.put(8, encodedSongList);     // add the song list data.

        DatagramPacket responseDatagram = new DatagramPacket(   responseBuffer.array(), responseBuffer.array().length,
                                                                lastPacketAddress, lastPacketPort);

        datagramSocket.send(responseDatagram);

    }

    // OPTIMIZATION: Packets range request could be optimized keeping the packetizer open
    //  to not re-open the file for each request (optimized with the session)
    /**
     * The range of packets [startPacketID, endPacketID) of the given filename, is sent to the client 
     */
    public void responseFilePacketsRange(String filename, short startPacketID, short endPacketID) throws IOException {

        Packetizer packetizer = new Packetizer((int)Protocol.PACKET_DATA_MAX_SIZE);
        packetizer.setFile(filename);
        packetizer.seekPacket(startPacketID);        

        // ##################### Send the packets #####################

        byte[] packetToSend = new byte[(int)Protocol.PACKET_DATA_MAX_SIZE + 2];
        DatagramPacket responseDatagram = new DatagramPacket(packetToSend, packetToSend.length, lastPacketAddress, lastPacketPort);

        int packetLenght;

        while ((startPacketID < endPacketID) && ((packetLenght = packetizer.getNextPacket(packetToSend, 2)) > 0)) {
            // add the data of the nPacket to the index 0 in BigEndian
            packetToSend[0] = (byte)(startPacketID >> 8);
            packetToSend[1] = (byte)startPacketID;

            // set the length of the packet and the data
            responseDatagram.setData(packetToSend, 0, packetLenght + 2);

            datagramSocket.send(responseDatagram);

            startPacketID++;
        }   

        packetizer.close();
    }

    /**
     * If the filename exists, the server sends a message to the client with the total size (in packets of PACKET_DATA_MAX_SIZE)
     *  of the given filename.
     * If the filename does not exist, the server sends 0
     * @param filename
     * @throws IOException
     */
    public void responseFilePacketsSize(String filename) throws IOException {
        // #####################  Send the total packets to be sent #####################
        Packetizer packetizer = new Packetizer((int)Protocol.PACKET_DATA_MAX_SIZE);

        short nPackets;

        if (packetizer.setFile(filename)) {
            nPackets = packetizer.getTotalPackets();
        }
        else {
            // the file does not exist
            nPackets = 0;
        }
        System.out.println("TOTAL PACKETS TO SEND: " + nPackets);
        
        byte[] byteBuffer = new byte[2];
        byteBuffer[0] = (byte)(nPackets >> 8);
        byteBuffer[1] = (byte)(nPackets & 0xFF);

        DatagramPacket responseDatagram = new DatagramPacket(   byteBuffer, 2,
                                                                lastPacketAddress, lastPacketPort);

        datagramSocket.send(responseDatagram);
    }

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
        private static final int DEFAULT_TTL = 100;
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
                        if(!lastSearch.startsWith(cond.toLowerCase())) { // If the condition contains less characters or is less than the initial condition ...
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
                    result.songList = searchEngine.getSongsWithCondition(cond);                          // Make the search.
                    result.cookie = allocateUser(cond, SongList.toString(result.songList));  // allocate the session in the hash map.
                                                                                          // Return the results of the search.
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
         * @return The cookie that identifies the session created for the user.
         * @pre The user must not exist in the list of searches.
         */
        private long allocateUser(String cond, String listOfSongs) {
            HistorialInfo histInfo = new HistorialInfo();
            Date actualDate = new Date();
            long cookie = generateCookie(actualDate, listOfSongs);
            histInfo.ttl = DEFAULT_TTL;
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
                        if(--value.ttl <= 0) {
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
}