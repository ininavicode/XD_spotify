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
        
        // | byte index | data             |
        // | ---------- | ---------------- |
        // | 0          | command type     |
        // | 1...1500   | name of the song |
        byte[] encodedString = song.toByteRaw();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + encodedString.length);
        
        byteBuffer.put(0, Protocol.COMMAND_TYPE.SONG_MP3_REQUEST.value);
        byteBuffer.put(1, encodedString);
        
        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);
                                                            
        // send the request
        socket.send(requestDatagram);
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

        // TODO: Implement the timeout
        // Receive the response from the server
        socket.receive(responseDatagram);

        // | byte index | data      |
        // | ---------- | --------- |
        // | 0...1      | n packets |

        byteBuffer = ByteBuffer.allocate(responseDatagram.getLength());
        byteBuffer.put(0, responseDatagram.getData(), 0, responseDatagram.getLength()); // get the data from the datagram

        short nPacketsToReceive = byteBuffer.getShort(0);   // get the n packets to receive

        // Step 1.2
        // reserve space for n packets of Protocol.MP3_PACKET_DATA_MAX_SIZE 
        byte[][] packetsList = new byte[nPacketsToReceive][(int)Protocol.MP3_PACKET_DATA_MAX_SIZE];
        for (int i = 0; i < nPacketsToReceive; i++) { // A for each cannot be used as copies to the values are accessed, instead of the references.
            packetsList[i] = null;
        }

        // Step 2
        socket.setSoTimeout(500); // 500 ms timeout
        short receivedCount = 0;
        boolean endReceiving = false;
        while (!endReceiving) {
            try {
                socket.receive(responseDatagram);

                // parse the id of the packet
                short packetID = (short) ((responseDatagram.getData()[0] << 8) | (responseDatagram.getData()[1] & 0xFF));

                if (packetsList[packetID] == null) {
                    receivedCount++;
                    // copy the data of the mp3 packet
                    packetsList[packetID] = Arrays.copyOfRange(responseDatagram.getData(), 2, responseDatagram.getLength());
                }

                if (receivedCount == nPacketsToReceive) {
                    endReceiving = true;
                }
                
                
            } catch (SocketTimeoutException e) {
                // if timeout, request the remaining packets and keep receiving
               
                for (int i = 0; i < packetsList.length; i++) {
                    if (packetsList[i] == null) {
                        // request a certain packet
                        RequestMP3Packet((short)i, song);
                    }
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


    private void RequestMP3Packet(short packetID, Song song) throws IOException {

        // | byte index | data         |
        // | ---------- | ------------ |
        // | 0          | command type |
        // | 1...2      | packet id    |
        // | 3...1500   | song name    |

        byte[] encodedString = song.toByteRaw();
        ByteBuffer byteBuffer = ByteBuffer.allocate(3 + encodedString.length);

        byteBuffer.put(0, Protocol.COMMAND_TYPE.SONG_MP3_PACKET_REQUEST.value);
        byteBuffer.putShort(1, packetID);
        byteBuffer.put(3, encodedString);

        // create the datagram
        DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                            byteBuffer.array().length,
                                                            serverAddress,
                                                            serverPort);

        // Enviem el paquet al servidor
        socket.send(requestDatagram);
    }

    // ##################### receiving #####################
    public Protocol.ResponseSearchEngine_t ReceiveSearchEngine() throws IOException {
        // Preparem per rebre la resposta del servidor
        byte[] buffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);

        // TODO: Implement the timeout
        // Receive the response from the server
        socket.receive(responseDatagram);

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
    
}