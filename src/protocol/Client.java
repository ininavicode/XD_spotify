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
    public Client(String serverAddress, int serverPort) throws UnknownHostException, SocketException {

        this.serverAddress = InetAddress.getByName(serverAddress);
        this.serverPort = serverPort;
    
        socket = new DatagramSocket();
    }

    // ##################### methods #####################
    public void close() {
        socket.close();
    }

    // ##################### getters #####################

    // ##################### requests #####################

    public void RequestSearchEngine(Protocol.RequestSearchEngine_t request) throws IOException {
        // | byte index | data                          |
        // | ---------- | ----------------------------- |
        // | 0          | command_type                  |
        // | 1...2      | client ID (signed 2 byte int) |
        // | 3...1500   | name to search                |

        byte[] encodedString = request.nameToSearch.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(3 + encodedString.length);
        byteBuffer.put(3, encodedString);                                   // add the encoded string
        byteBuffer.putShort(1, request.clientID);                           // add the session ID
        byteBuffer.put(0, Protocol.COMMAND_TYPE.SEARCH_ENGINE_REQUEST.value);  // add the command type
        
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
     * The method sends a request to the server to get the given song, so the client expects a stream of packets containing
     *  the data of the specifyied song at the request
     * @param filename : Name of the file to create -> "filename"
     * @param song : Song requested at previous RequestMP3
     */
    public void RequestReceiveMP3(Song song, String filename) throws IOException {
        // request (start) ++++++++++++++++++++++++++++++++

        RequestNPacketsOfSong(song);
        short nPacketsToReceive;

        socket.setSoTimeout(20);    // 20 ms timeout

        while (true) { 
            try {
                nPacketsToReceive = ReceiveNPacketsOfSong();    // 20 ms timeout
                break;
                
            } catch (SocketTimeoutException e) {
                
            }
        }
        
        // request (end)   --------------------------------

        // receive (start) ++++++++++++++++++++++++++++++++
        
        // 1. The client expects a message containing the total count of packets to receive (Protocol.DATAGRAM_DATA_MAX_SIZE each packet)
        // 1.1 If the number of the message is 0, that means that the song does not exist 
        //  (we will not treat this case, as the application doesn't allow the client to request an unexisting song)
        // 1.2 Reserve memory for all the packets to receive
        // REVIEW: Analize what happens if the client does not receive the message with the n packets to receive
        // 2. Enter into a loop, storing each packet received into the reserved list at the step 2
        // 3. Once the timeout is on, check if all the packets at the list are not null, if any packet is null,
        //  request it to the server

        byte[] buffer = new byte[1500];    // in theory, the sent number is an unsigned short, but for later it should be assigned with greater memory.
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // Step 1.2
        // reserve space for n packets of Protocol.MP3_PACKET_DATA_MAX_SIZE 
        byte[][] packetsList = new byte[nPacketsToReceive][(int)Protocol.MP3_PACKET_DATA_MAX_SIZE];
        for (int i = 0; i < nPacketsToReceive; i++) { // A for each cannot be used as copies to the values are accessed, instead of the references.
            packetsList[i] = null;
        }

        // Step 2
        socket.setSoTimeout(20); // 20 ms timeout
        short receivedCount = 0;
        boolean endReceiving = false;

        short streamReceivedCount = 0;
        int nStream = 0;

        final short streamSize = 1000;
        RequestMP3PacketRange((short)0, (short)streamSize, song);

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
                    RequestMP3PacketRange((short)(nStream * streamSize), (short)((nStream + 1) * streamSize), song);

                }
                
                
            } catch (SocketTimeoutException e) {
                // if timeout, request the remaining packets and keep receiving
                // {null, 1, 1, null, null}
                short startID = (short)(nStream * streamSize), endID;
                boolean end = false;

                short streamEnd = (short)((nStream + 1) * streamSize);

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
    
                    if (startID < streamEnd) RequestMP3PacketRange(startID, endID, song);
                    

                    startID = endID;
                }
                
            }

        }
        socket.setSoTimeout(0); // unable the timeout
        // receive (end)   --------------------------------
        
        // ##################### create the file #####################
        FileFromPacket fileFromPacket = new FileFromPacket(filename);

        for (byte[] packet : packetsList) {
            fileFromPacket.appendPacket(packet, 0, packet.length);
        }

        fileFromPacket.close();


    }    


    private void RequestMP3PacketRange(short startPacketID, short endPacketID, Song song) throws IOException {


        // | byte index | data                         |
        // | ---------- | ---------------------------- |
        // | 0          | command type                 |
        // | 1...2      | packet id start (included)   |
        // | 3...4      | packet id end (not included) |
        // | 5...1500   | song name                    |
        
        byte[] encodedString = song.toByteRaw();
        ByteBuffer byteBuffer = ByteBuffer.allocate(5 + encodedString.length);

        byteBuffer.put(0, Protocol.COMMAND_TYPE.SONG_MP3_PACKETS_RANGE_REQUEST.value);
        byteBuffer.putShort(1, startPacketID);
        byteBuffer.putShort(3, endPacketID);
        byteBuffer.put(5, encodedString);

        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);

        // Enviem el paquet al servidor
        socket.send(requestDatagram);
    }

    private void RequestNPacketsOfSong(Song song) throws IOException {
        // | byte index | data             |
        // | ---------- | ---------------- |
        // | 0          | command type     |
        // | 1...1500   | name of the song |
        
        byte[] encodedString = song.toByteRaw();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + encodedString.length);
        
        byteBuffer.put(0, Protocol.COMMAND_TYPE.SONG_MP3_N_PACKETS_REQUEST.value);
        byteBuffer.put(1, encodedString);
        
        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);
                                                            
        // send the request
        socket.send(requestDatagram);
    }

    // ##################### receiving #####################
    public Protocol.ResponseSearchEngine_t ReceiveSearchEngine() throws IOException {
        // Preparem per rebre la resposta del servidor
        byte[] buffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // Receive the response from the server
        socket.setSoTimeout(20);    // 20 ms timeout
        while (true) { 
            try {
                socket.receive(responseDatagram);
                socket.setSoTimeout(0);    // disable the timeout
                break;
                
            } catch (SocketTimeoutException e) {
                
            }
        }

        // | byte index | data                                      |
        // | ---------- | ----------------------------------------- |
        // | 0...1      | client ID assignement (signed 2 byte int) |
        // | 2...1500   | results of the search (10 songs)          |

        ByteBuffer byteBuffer = ByteBuffer.allocate(responseDatagram.getLength());
        byteBuffer.put(0, responseDatagram.getData(), 0, responseDatagram.getLength()); // get the data from the datagram

        Protocol.ResponseSearchEngine_t response = new Protocol.ResponseSearchEngine_t();

        response.clientID = byteBuffer.getShort(0);
        byte[] songsStr = new byte[responseDatagram.getLength() - 2];   // parse the 10 songs
        byteBuffer.get(2, songsStr);                                    // ...
        response.songList = SongList.fromByteRaw(songsStr);             // ...

        return response;
    }


    /**
     * 
     * @return The packets of the requested song with RequestNPacketsOfSong
     * @throws IOException
     */
    private short ReceiveNPacketsOfSong() throws IOException, SocketTimeoutException {
        byte[] buffer = new byte[2];    // in theory, the sent number is an unsigned short, but for later it should be assigned with greater memory.
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // Receive the response from the server
        socket.receive(responseDatagram);

        // | byte index | data      |
        // | ---------- | --------- |
        // | 0...1      | n packets |

        short output = (short)((responseDatagram.getData()[0] << 8) | (responseDatagram.getData()[1] & 0xFF));

        return output;
    }
   
    
}