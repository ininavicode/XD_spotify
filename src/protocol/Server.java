package protocol;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import packetizer.Packetizer;
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

    private SessionHandler sessionHandler;

    // ##################### constructors #####################
    /**
     * Initialize a server with the given port
     * @param serverPort
     * @param fileName The filename of the list of available songs in the server.
     * @throws IOException
     */
    public Server(int serverPort, String fileName) throws IOException {
        this.serverPort = serverPort;
        sessionHandler = new SessionHandler(fileName);
        datagramPacketBuffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        datagramPacket = new DatagramPacket(datagramPacketBuffer, (int)Protocol.DATAGRAM_MAX_SIZE);

        datagramSocket = new DatagramSocket(this.serverPort);
    }

    // ##################### methods #####################
    /**
     * Waits for any packet, once receives, parses the information of it and stores the data to be accessible later
     * @throws IOException
     */
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
    /**
     * Gets the command type of the last received packet
     * @return The command type
     */
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
     * Gets automatically song name of the last packet, in function of the last
     *  command type
     * @return Song name of the last packet
     */
    public String getLastPacket_SongName() {
        byte[] encodedStr;

        // ##################### SONG_MP3_N_PACKETS_REQUEST #####################
        if (lastPacketCommandType == Protocol.COMMAND_TYPE.SONG_MP3_N_PACKETS_REQUEST) {
            encodedStr = new byte[lastPacketData.length - 8];
            lastPacketByteBuffer.get(8, encodedStr, 0, encodedStr.length);
            return new String(encodedStr);

        }

        // ############ SEARCH_ENGINE_REQUEST ############
        
        else if (lastPacketCommandType == Protocol.COMMAND_TYPE.SEARCH_ENGINE_REQUEST) {
            encodedStr = new byte[lastPacketData.length - 8]; 
            lastPacketByteBuffer.get(8, encodedStr, 0, encodedStr.length);

            return new String(encodedStr);

        }

        // // ############ SONG_MP3_PACKETS_RANGE_REQUEST ############

        // else if (lastPacketCommandType == Protocol.COMMAND_TYPE.SONG_MP3_PACKETS_RANGE_REQUEST) {
        //     encodedStr = new byte[lastPacketData.length - 4];
        //     lastPacketByteBuffer.get(4, encodedStr, 0, encodedStr.length);

        //     return new String(encodedStr);
        // }

        return null;

    }

    // ############ SEARCH_ENGINE_REQUEST ############
    /**
     * @return The cookie value of the last received packet
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

    // ############ SONG_MP3_PACKETS_RANGE_REQUEST ############
    /**
     * @return The start packet ID of the last received packet
     * @pre Last command type must be SONG_MP3_PACKETS_RANGE_REQUEST
     */
    public short getLastPacket_StartPacketID() {

        // | byte index | data                         |
        // | ---------- | ---------------------------- |
        // | 0          | command type                 |
        // | 1...8      | cookie session               |
        // | 9...10     | packet id start (included)   |
        // | 11...12    | packet id end (not included) |

        return lastPacketByteBuffer.getShort(8);
    }

    /**
     * @return The end packet ID of the last received packet
     * @pre Last command type must be SONG_MP3_PACKETS_RANGE_REQUEST
     */
    public short getLastPacket_EndPacketID() {

        // | byte index | data                         |
        // | ---------- | ---------------------------- |
        // | 0          | command type                 |
        // | 1...8      | cookie session               |
        // | 9...10     | packet id start (included)   |
        // | 11...12    | packet id end (not included) |

        return lastPacketByteBuffer.getShort(10);
    }

    // ##################### responses #####################
    /**
     * Sends the given search engine response (song list, cookie), to the source address of the last received packet
     * @throws IOException
     */
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
     * Sends the range of packets [startPacketID, endPacketID) of the given filename, to the source address of the 
     *  last received packet
     * @param cookie        session cookie of sending mp3 info.
     * @param startPacketID Included
     * @param endPacketID   Not included
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void responseFilePacketsRange(long cookie, short startPacketID, short endPacketID) throws IOException, FileNotFoundException {
        String filename;
        if((filename = sessionHandler.retreiveSongMP3Name(cookie)) != null) {
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
        } // if it is null, the session doesn't exist, this should not be happening by any means.
    }

    /**
     * If the filename exists, the server sends a message to the client with the total size (in packets of PACKET_DATA_MAX_SIZE)
     * of the given filename, and the cookie that will hold the session until all the song is correctly sent.
     * If the filename does not exist, the server sends 0
     * Additionally, a session for the sending of packets is generated, so the name song must not be sent infinitely.
     * A cookie is returned in the packet, a valid number session if generated, -1 otherwise.
     * 
     * @param filename
     * @param cookie The actual session cookie identifier.
     * @pre This method has to be called after receiving a petition of file packets size.
     * @throws IOException
     */
    public void responseFilePacketsSize(String filename) throws IOException {
        // #####################  Send the total packets to be sent #####################
        Packetizer packetizer = new Packetizer((int)Protocol.PACKET_DATA_MAX_SIZE);

        short nPackets;
        long newCookie;

        try {
            packetizer.setFile(filename);
            nPackets = packetizer.getTotalPackets();
            newCookie = sessionHandler.generateSongRequestUser(getLastPacket_Cookie(), filename);
            
        } catch (FileNotFoundException e) {
            nPackets = 0;
            newCookie = -1; // and session is not generated.

        }
        System.out.println("TOTAL PACKETS TO SEND: " + nPackets);
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(80); // 16 bits for nPackets at its maximum, 64 bits for cookie.
        byteBuffer.putShort(0, nPackets);
        byteBuffer.putLong(2, newCookie);

        DatagramPacket responseDatagram = new DatagramPacket(   byteBuffer.array(), 80,
                                                                lastPacketAddress, lastPacketPort);

        datagramSocket.send(responseDatagram);
    }

    /**
     * Method that makes a search in the session handler class.
     * 
     * @param cookie The number of session (-1 if it is not assigned, or a long value containing the
     *               last session assigned to the client).
     * @param cond The search condition.
     * @return A response search engine type (the cookie, possibly different from original, and the list
     *         of best matching songs available).
     * @pre A search engine request must have been received before invoking this method.
     */
    public Protocol.ResponseSearchEngine_t makeSearch() {
        return sessionHandler.makeSearch(this.getLastPacket_Cookie(), this.getLastPacket_SongName());
    }

    /**
     * Method to close an active session by brutal force, without a timeout.
     * 
     * @param cookie The cookie of the session to be closed.
     */
    public void closeSession(long cookie) {
        sessionHandler.closeSession(cookie);
    }

}