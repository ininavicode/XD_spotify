package protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import song.*;

public class Protocol {
    private static final long PACKET_MAX_SIZE = 1500;
    private static  final long MP3_PACKET_SIZE = 1498; // 1500 - 2

    public static enum COMMAND_TYPE {

        INVALID_COMMAND((byte)0),
        SEARCH_ENGINE_REQUEST((byte)1),
        SONG_MP3_REQUEST((byte)2),
        SONG_MP3_PACKET_REQUEST((byte)3);
        
        private COMMAND_TYPE(byte value) {
            this.value = value;
        }
        
        public final byte value;
        
        public static COMMAND_TYPE fromByte(byte value) {
            if (value == SEARCH_ENGINE_REQUEST.value) return SEARCH_ENGINE_REQUEST;
            else if (value == SONG_MP3_REQUEST.value) return SONG_MP3_REQUEST;
            else if (value == SONG_MP3_PACKET_REQUEST.value) return SONG_MP3_PACKET_REQUEST;
            else return INVALID_COMMAND;
            
        }
    }

    static public class Client {
        private InetAddress serverAddress;
        private int serverPort;
        private DatagramSocket socket;

        private short clientID; // to establish a session with the server

        // ##################### constructors #####################
        public Client(String serverAddress, int serverPort) throws UnknownHostException, SocketException {

            this.serverAddress = InetAddress.getByName(serverAddress);
            this.serverPort = serverPort;
        
            clientID = -1;  // set session to not stablished
            socket = new DatagramSocket();
        }

        // ##################### methods #####################
        public void close() {
            socket.close();
        }

        // ##################### getters #####################
        public short getClientID() {
            return clientID;
        }
        // ##################### requests #####################

        public void Request_searchEngine(String nameToSearch) throws IOException {
            // | byte index | data                          |
            // | ---------- | ----------------------------- |
            // | 0          | command_type                  |
            // | 1...2      | client ID (signed 2 byte int) |
            // | 3...1500   | name to search                |

            byte[] encodedString = nameToSearch.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(3 + encodedString.length);
            byteBuffer.put(3, encodedString);                           // add the encoded string
            byteBuffer.putShort(1, clientID);                           // add the session ID
            byteBuffer.put(COMMAND_TYPE.SEARCH_ENGINE_REQUEST.value);   // add the command type
            
            // create the datagram
            DatagramPacket requestDatagram = new DatagramPacket(  byteBuffer.array(),
                                                                byteBuffer.array().length,
                                                                serverAddress,
                                                                serverPort);

            // Enviem el paquet al servidor
            socket.send(requestDatagram);
        }

        // ##################### receiving #####################
        public ArrayList<Song> Receive_searchEngine() throws IOException {
            // Preparem per rebre la resposta del servidor
            byte[] buffer = new byte[(int)PACKET_MAX_SIZE];
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
            clientID = byteBuffer.getShort(0);          // parse the clientID

            byte[] songsStr = new byte[responseDatagram.getLength() - 2];   // parse the 10 songs
            byteBuffer.get(2, songsStr);                                    // ...
            ArrayList<Song> songsList = SongList.fromByteRaw(songsStr);     // ...

            return songsList;
        }

        // TODO: all the other methods
    }

    static public class Server {
        private int serverPort;
        private DatagramSocket datagramSocket;

        private DatagramPacket datagramPacket;
        private final byte[] datagramPacketBuffer;

        private InetAddress lastPacketAddress;
        private int lastPacketPort;
        private COMMAND_TYPE lastPacketCommandType;
        private byte[] lastPacketData;
        private ByteBuffer lastPacketByteBuffer;

        // ##################### constructors #####################
        public Server(int serverPort) throws IOException {
            this.serverPort = serverPort;
            datagramPacketBuffer = new byte[(int)PACKET_MAX_SIZE];
            datagramPacket = new DatagramPacket(datagramPacketBuffer, (int)PACKET_MAX_SIZE);

            datagramSocket = new DatagramSocket(serverPort);
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
            lastPacketCommandType = COMMAND_TYPE.fromByte(lastPacketByteBuffer.array()[0]);

            // save the data of the packet
            // the first byte of a request is allways the command type, so the data is from the byte 1 to the end
            lastPacketData = new byte[lastPacketByteBuffer.array().length - 1];
            lastPacketByteBuffer.get(1, lastPacketData);
            
            // save the data to post process it without the command type 
            lastPacketByteBuffer.put(0, datagramPacket.getData(), 1, datagramPacket.getLength() - 1);

        }


        // ##################### getters #####################
        public COMMAND_TYPE getLastPacketCommandType() {
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

            // ############ SONG_MP3_REQUEST ############
            if (lastPacketCommandType == COMMAND_TYPE.SONG_MP3_REQUEST) {
                return new String(lastPacketData);

            }

            // ############ SEARCH_ENGINE_REQUEST ############
            // ############ SONG_MP3_PACKET_REQUEST ############
            else if (lastPacketCommandType == COMMAND_TYPE.SEARCH_ENGINE_REQUEST) {
                encodedStr = new byte[lastPacketData.length - 2];
                lastPacketByteBuffer.get(2, encodedStr, 0, encodedStr.length);
    
                return new String(encodedStr);

            }

            return null;

        }

        // ############ SEARCH_ENGINE_REQUEST ############
        /**
         * @pre Last command type must be SEARCH_ENGINE_REQUEST
         */
        public short getLastPacket_ClientID() {
            return lastPacketByteBuffer.getShort(0);
        }

        /**
         * @pre Last command type must be SONG_MP3_PACKET_REQUEST
         */
        // ############ SONG_MP3_PACKET_REQUEST ############
        public short getLastPacket_PacketID() {
            return lastPacketByteBuffer.getShort(0);
        }


        // ##################### responses #####################
        public void Response_searchEngine(short assignedClientID, ArrayList<Song> songList) throws IOException {
            byte[] encodedSongList = SongList.toByteRaw(songList);

            ByteBuffer responseBuffer = ByteBuffer.allocate(encodedSongList.length + 2);    // data + clientID

            responseBuffer.putShort(assignedClientID);  // add the clientID
            responseBuffer.put(2, encodedSongList);     // add the song list data

            DatagramPacket responseDatagram = new DatagramPacket(   responseBuffer.array(), responseBuffer.array().length,
                                                                    lastPacketAddress, lastPacketPort);

            datagramSocket.send(responseDatagram);

        }
        
    }
}
