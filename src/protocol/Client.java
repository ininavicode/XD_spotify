package protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import packetizer.FileFromPacket;
import song.*;


public class Client {
    private InetAddress serverAddress;
    private int serverPort;
    private DatagramSocket socket;

    // ##################### constructors #####################
    /**
     * Initializes the client with the given server parameters
     * @param serverAddress
     * @param serverPort
     * @throws UnknownHostException
     * @throws SocketException
     */
    public Client(String serverAddress, int serverPort) throws UnknownHostException, SocketException {

        this.serverAddress = InetAddress.getByName(serverAddress);
        this.serverPort = serverPort;
    
        socket = new DatagramSocket();
    }

    // ##################### methods #####################
    /**
     * Closes the client, this includes closing the socket
     */
    public void close() {
        socket.close();
    }

    // ##################### getters #####################

    // ##################### requests #####################

    /**
     * This method executes a request to the server to receive a song list with the method receiveSearchEngine,
     * expecting the results of the search engine in function of the given request to this method.
     * This method is used to stablish session too, with the clientID
     * @param nameToSearch : String that will arrive to the search engine (nameToSearch.getBytes().length <= 1450)
     * @throws IOException
     * @throws IllegalArgumentException : If (nameToSearch.getBytes().length > 1450) 
     * 
     */
    public void requestSearchEngine(String nameToSearch, long cookie) throws IOException, IllegalArgumentException {
        // | byte index     | data                          |
        // | -------------- | ----------------------------- |
        // | 0              | command_type                  |
        // | 1...8          | cookie                        |
        // | 9...9+1450-1   | name to search                |

        byte[] encodedString = nameToSearch.getBytes();

        if (encodedString.length > 1450) {
            throw new IllegalArgumentException("nameToSearch length exceeded");
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(9 + encodedString.length);
        byteBuffer.put(9, encodedString);                                       // add the encoded string
        byteBuffer.putLong(1, cookie);                                          // add the session ID
        byteBuffer.put(0, Protocol.COMMAND_TYPE.SEARCH_ENGINE_REQUEST.value);   // add the command type
        
        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);

        // Enviem el paquet al servidor
        socket.send(requestDatagram);
    }

    /**
     * This method creates a new file with the given filename
     * @param clientCookie The last available client cookie for the session.
     * @param filename : Name of the file to create -> "filename"
     * @param song : Song to request to the server.
     * @return True if the requested song exists. False if not.
     * @pre At this version, the file to receive can not be longer than (2^16 * 1450 B = 95.02 MB), due the 
     * limitation of 2 bytes for packetID (max for packetID = 2^16 different values)
     */
    public boolean requestReceiveFile(long clientCookie, Song song, String filename) throws IOException {
        // request (start) ++++++++++++++++++++++++++++++++

        requestFilePacketsSize(song, clientCookie);
        short nPacketsToReceive;
        long cookieOfSendingSession;
        ByteBuffer response;

        socket.setSoTimeout(300);    // 300 ms timeout

        while (true) { 
            try {
                response = receiveFilePacketsSize();    // 20 ms timeout
                break;
                
            } catch (SocketTimeoutException e) {
                
            }
        }

        // | byte index | data      |
        // | ---------- | --------- |
        // | 0...1      | n packets |
        // | 2...9      | cookie    |

        nPacketsToReceive = response.getShort();
        cookieOfSendingSession = response.getLong(2);
        

        // if the packet responses with 0, that means that the requested song does not exist
        if (nPacketsToReceive == 0) {
            return false;
        }
        
        // request (end)   --------------------------------

        // receive (start) ++++++++++++++++++++++++++++++++
        
        // 1. The client expects a message containing the total count of packets to receive (Protocol.DATAGRAM_DATA_MAX_SIZE each packet)
        // 1.1 If the number of the message is 0, that means that the song does not exist 
        //  (we will not treat this case, as the application doesn't allow the client to request an unexisting song)
        // 1.2 Reserve memory for all the packets to receive

        byte[] buffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // Step 1.2
        // reserve space for n packets of Protocol.PACKET_DATA_MAX_SIZE bytes
        byte[][] packetsList = new byte[nPacketsToReceive][(int)Protocol.PACKET_DATA_MAX_SIZE];
        for (int i = 0; i < nPacketsToReceive; i++) { // A for each cannot be used as copies to the values are accessed, instead of the references.
            packetsList[i] = null;
        }

        // Step 2
        socket.setSoTimeout(100); // 20 ms timeout
        short receivedCount = 0;
        boolean endReceiving = false;

        short streamReceivedCount = 0;
        int nStream = 0;

        // REVIEW: Analize streamSize
        final short streamSize = 1000;
        requestFilePacketsRange((short)0, (short)streamSize, cookieOfSendingSession);

        while (!endReceiving) {
            try {
                socket.receive(responseDatagram);

                // parse the id of the packet
                short packetID = (short) ((responseDatagram.getData()[0] << 8) | (responseDatagram.getData()[1] & 0xFF));

                if (packetsList[packetID] == null) {
                    receivedCount++;
                    streamReceivedCount++;
                    // copy the data of the mp3 packet
                    packetsList[packetID] = Arrays.copyOfRange(responseDatagram.getData(), 2, responseDatagram.getLength());
                }

                if (receivedCount >= nPacketsToReceive) {
                    endReceiving = true;
                }
                else if (streamReceivedCount >= 1000) {
                    streamReceivedCount = 0;
                    nStream++;
                    // request the next 1000 packets
                    requestFilePacketsRange((short)(nStream * streamSize), (short)((nStream + 1) * streamSize), cookieOfSendingSession);

                }
                
                
            } catch (SocketTimeoutException e) {
                // if timeout, request the remaining packets and keep receiving
                // {null, 1, 1, null, null}
                short startID = (short)(nStream * streamSize), endID;
                boolean end = false;

                short streamEnd = (short)Math.min(nPacketsToReceive, (short)((nStream + 1) * streamSize));

                while (!end) {

                    while (!end && (packetsList[startID] != null)) {
                        startID++;
                        if (startID >= streamEnd) end = true;
                    }
    
                    endID = (short)(startID + 1);
                    if (endID >= packetsList.length) end = true;
    
                    while (!end && (packetsList[endID] == null)) {
                        endID++;
                        if (endID >= streamEnd) end = true;
                    }
    
                    if (startID < streamEnd) requestFilePacketsRange(startID, endID, cookieOfSendingSession);
                    

                    startID = endID;
                }
                
            }

        }
        socket.setSoTimeout(0); // unable the timeout

        communicateEndOfReceiving(cookieOfSendingSession); // The end of receiving must be communicated.
        // receive (end)   --------------------------------
        
        // ##################### create the file #####################
        FileFromPacket fileFromPacket = new FileFromPacket(filename);

        for (byte[] packet : packetsList) {
            fileFromPacket.appendPacket(packet, 0, packet.length);
        }

        fileFromPacket.close();

        // notify that the file was parsed succesfully
        return true;
    }    

    /**
     * Requests to the server the given range of packets (startPacketID included, endPacketID excluded), of the
     * given song, the name of the song is traduced at the server to search is respective .mp3 file
     * @param startPacketID Included to receive
     * @param endPacketID   Excluded to receive
     * @param cookie        The cookie of the session where the name of the song is held in the server.
     * @pre The given song must exist at the server
     */
    private void requestFilePacketsRange(short startPacketID, short endPacketID, long cookie) throws IOException {


        // | byte index | data                         |
        // | ---------- | ---------------------------- |
        // | 0          | command type                 |
        // | 1...8      | cookie session               |
        // | 9...10     | packet id start (included)   |
        // | 11...12    | packet id end (not included) |
        
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(13);

        byteBuffer.put(0, Protocol.COMMAND_TYPE.SONG_MP3_PACKETS_RANGE_REQUEST.value);
        byteBuffer.putLong(1, cookie);
        byteBuffer.putShort(9, startPacketID);
        byteBuffer.putShort(11, endPacketID);

        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);

        // Send the packet to server.
        socket.send(requestDatagram);
    }

    /**
     * Requests to the server to send a response with the number of packets of 1450 bytes
     * of the respective .mp3 of the given song
     * @param song
     */
    private void requestFilePacketsSize(Song song, long cookie) throws IOException {
        // | byte index | data             |
        // | ---------- | ---------------- |
        // | 0          | command type     |
        // | 1...8      | cookie           |
        // | 9...1500   | name of the song |
        
        byte[] encodedString = song.toByteRaw();
        ByteBuffer byteBuffer = ByteBuffer.allocate(9 + encodedString.length);
        
        byteBuffer.put(0, Protocol.COMMAND_TYPE.SONG_MP3_N_PACKETS_REQUEST.value);
        byteBuffer.putLong(1, cookie);
        byteBuffer.put(9, encodedString);
        
        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);
                                                            
        // send the request
        socket.send(requestDatagram);
    }

    // ##################### receiving #####################
    /**
     * Receives the response of the request sent with requestSearchEngine
     * @return The response of the server
     */
    public Protocol.ResponseSearchEngine_t receiveSearchEngine() throws IOException {
        // Preparem per rebre la resposta del servidor
        byte[] buffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // Receive the response from the server
        socket.setSoTimeout(300);    // 300 ms timeout
        while (true) { 
            try {
                socket.receive(responseDatagram);
                socket.setSoTimeout(0);    // disable the timeout
                break;
                
            } catch (SocketTimeoutException e) {
                
            }
        }

        // | byte index | data                             |
        // | ---------- | -------------------------------- |
        // | 0 .. 7     | cookie                           |
        // | 7 .. 1500  | results of the search (10 songs) |

        ByteBuffer byteBuffer = ByteBuffer.allocate(responseDatagram.getLength());
        byteBuffer.put(0, responseDatagram.getData(), 0, responseDatagram.getLength()); // get the data from the datagram

        Protocol.ResponseSearchEngine_t response = new Protocol.ResponseSearchEngine_t();

        response.cookie = byteBuffer.getLong(0);
        byte[] songsStr = new byte[responseDatagram.getLength() - 8];   // parse the 10 songs
        byteBuffer.get(8, songsStr);                                    // ...
        response.songList = SongList.fromByteRaw(songsStr);             // ...

        return response;
    }


    /**
     * 
     * @return The information received from the request, containing the number of packets and cookie session.
     * @throws IOException
     */
    private ByteBuffer receiveFilePacketsSize() throws IOException, SocketTimeoutException {
        byte[] buffer = new byte[80];
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // Receive the response from the server
        socket.receive(responseDatagram);

        return ByteBuffer.wrap(buffer);
    }

    /**
     * Method that indicates a server to finish a session.
     * 
     * @param cookie The cookie session to be ended.
     */
    private void communicateEndOfReceiving(long cookie) throws IOException{
        // | byte index | data                             |
        // | ---------- | -------------------------------- |
        // | 0          | command type                     |
        // | 1 . 8      | cookie session number            |
        ByteBuffer response = ByteBuffer.allocate(9);
        response.put(Protocol.COMMAND_TYPE.FINISH_COMM.value);
        response.putLong(1, cookie);
        DatagramPacket responsePacket = new DatagramPacket(response.array(), 9,
                                            serverAddress,
                                            serverPort);
        socket.send(responsePacket);
    }   
   
    
}