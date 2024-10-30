package protocol;

import java.util.ArrayList;
import song.*;

/**
 * This class stores constants to handle the behaviour of the Client/Server classes
 */

public class Protocol {
    public static final long DATAGRAM_MAX_SIZE = 1500;
    public static  final long MP3_PACKET_DATA_MAX_SIZE = 1450; // 1500 - 50
    public static  final long DATAGRAM_DATA_MAX_SIZE = 1453; // 1500 - 50 + commandType.lenght + packetID.length

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

    // ##################### nested classes #####################
    /*
     * The following classes are input/output data types for the different request/receive methods
     */

    // ##################### SearchEngine #####################
    public static class RequestSearchEngine_t {
        public short clientID;              // 2 bytes
        public String nameToSearch;         // rest of bytes

        public RequestSearchEngine_t(short clientID, String nameToSearch) {
            this.clientID = clientID;
            this.nameToSearch = nameToSearch;
        }

        public RequestSearchEngine_t() {

        }
    }

    public static class ResponseSearchEngine_t {
        public short clientID;         // 2 bytes    
        public ArrayList<Song> songList;    // rest of bytes (10 songs)

        public ResponseSearchEngine_t(short clientID, ArrayList<Song> songList) {
            this.clientID = clientID;
            this.songList = songList;
        }

        public ResponseSearchEngine_t() {

        }
    }

    // ##################### MP3 #####################
 

    // ##################### MP3 Packet #####################

    public static class RequestMP3Packet_t {
        public short packetID;
        public String songName;

        public RequestMP3Packet_t(short packetID, String songName) {
            this.packetID = packetID;
            this.songName = songName;
        }

        public RequestMP3Packet_t() {

        }
    }

    public static class ReceiveMP3Packet_t {
        public short packetID;
        public byte[] data;

        public ReceiveMP3Packet_t(short packetID, byte[] data) {
            this.packetID = packetID;
            this.data = data;
        }

        public ReceiveMP3Packet_t() {

        }
    }

}
