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

        // ############ SONG_MP3_REQUEST ############
        if (lastPacketCommandType == Protocol.COMMAND_TYPE.SONG_MP3_REQUEST) {
            return new String(lastPacketData);

        }

        // ############ SEARCH_ENGINE_REQUEST ############
        // ############ SONG_MP3_PACKET_REQUEST ############
        else if (   lastPacketCommandType == Protocol.COMMAND_TYPE.SEARCH_ENGINE_REQUEST ||
                    lastPacketCommandType == Protocol.COMMAND_TYPE.SONG_MP3_PACKET_REQUEST) {
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

        // | byte index | data         |
        // | ---------- | ------------ |
        // | 0          | command type |
        // | 1...2      | packet id    |
        // | 3...1500   | song name    |

        return lastPacketByteBuffer.getShort(0);
    }


    // ##################### responses #####################
    public void ResponseSearchEngine(Protocol.ResponseSearchEngine_t response) throws IOException {
        byte[] encodedSongList = SongList.toByteRaw(response.songList);

        ByteBuffer responseBuffer = ByteBuffer.allocate(encodedSongList.length + 2);    // data + clientID

        responseBuffer.putShort(response.clientID);  // add the clientID
        responseBuffer.put(2, encodedSongList);     // add the song list data

        DatagramPacket responseDatagram = new DatagramPacket(   responseBuffer.array(), responseBuffer.array().length,
                                                                lastPacketAddress, lastPacketPort);

        datagramSocket.send(responseDatagram);

    }

    public void ResponseMP3(String filename) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate((int)Protocol.MP3_PACKET_DATA_MAX_SIZE + 2);

        Packetizer packetizer = new Packetizer((int)Protocol.MP3_PACKET_DATA_MAX_SIZE);
        packetizer.setFile(filename);

        // #####################  Send the total packets to be sent #####################
        byteBuffer.putShort(0, packetizer.getTotalPackets());

        DatagramPacket responseDatagram = new DatagramPacket(   byteBuffer.array(), 2,
                                                                lastPacketAddress, lastPacketPort);

        datagramSocket.send(responseDatagram);

        // ##################### Send the packets #####################

        byte[] packetToSend = new byte[(int)Protocol.MP3_PACKET_DATA_MAX_SIZE + 2];
        short nPacket = 0;
        int packetLenght;

        while ((packetLenght = packetizer.getNextPacket(packetToSend, 2)) > 0) {
            // add the data of the nPacket to the index 0 in BigEndian
            packetToSend[0] = (byte)(nPacket >> 8);
            packetToSend[1] = (byte)nPacket;

            // set the length of the packet and the data
            responseDatagram.setData(packetToSend, 0, packetLenght + 2);

            datagramSocket.send(responseDatagram);

            nPacket++;
        }   

        packetizer.close();
        
    }

    // OPTIMIZATION: Specific packet request could be optimized keeping the packetizer open
    //  to not re-open the file for each request (optimized with the clientID)
    public void responseMP3Packet(String filename, int packetID) throws IOException {
        // ##################### Send the packets #####################
        Packetizer packetizer = new Packetizer((int)Protocol.MP3_PACKET_DATA_MAX_SIZE);
        packetizer.setFile(filename);

        byte[] packetToSend = new byte[(int)Protocol.MP3_PACKET_DATA_MAX_SIZE + 2];
        int packetLenght;

        packetLenght = packetizer.getNthPacket(packetID, packetToSend, 2);
        // add the data of the nPacket to the index 0 in BigEndian
        packetToSend[0] = (byte)(packetID >> 8);
        packetToSend[1] = (byte)packetID;

        DatagramPacket responseDatagram = new DatagramPacket(packetToSend, packetLenght + 2, lastPacketAddress, lastPacketPort);
        responseDatagram.setData(packetToSend);
        responseDatagram.setLength(packetLenght);

        datagramSocket.send(responseDatagram);

        packetizer.close();
    }
    
}