package protocol;

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

    // ##################### constructors #####################
    public Server(int serverPort) throws IOException {
        this.serverPort = serverPort;
        datagramPacketBuffer = new byte[(int)Protocol.DATAGRAM_MAX_SIZE];
        datagramPacket = new DatagramPacket(datagramPacketBuffer, (int)Protocol.DATAGRAM_MAX_SIZE);

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
        // ############ SONG_MP3_PACKETS_RANGE_REQUEST ############
        else if (lastPacketCommandType == Protocol.COMMAND_TYPE.SEARCH_ENGINE_REQUEST) {
            encodedStr = new byte[lastPacketData.length - 2];
            lastPacketByteBuffer.get(2, encodedStr, 0, encodedStr.length);

            return new String(encodedStr);

        }
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
    public short getLastPacket_ClientID() {
        return lastPacketByteBuffer.getShort(0);
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

        ByteBuffer responseBuffer = ByteBuffer.allocate(encodedSongList.length + 2);    // data + clientID

        responseBuffer.putShort(response.clientID);  // add the clientID
        responseBuffer.put(2, encodedSongList);     // add the song list data

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
        
        byte[] byteBuffer = new byte[2];
        byteBuffer[0] = (byte)(nPackets >> 8);
        byteBuffer[1] = (byte)(nPackets & 0xFF);

        DatagramPacket responseDatagram = new DatagramPacket(   byteBuffer, 2,
                                                                lastPacketAddress, lastPacketPort);

        datagramSocket.send(responseDatagram);
    }
    
}