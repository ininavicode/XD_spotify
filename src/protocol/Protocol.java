package protocol;

import java.util.ArrayList;
import song.*;

/**
 * This class stores constants to handle the behaviour of the Client/Server classes
 */

public class Protocol {
    public static final long DATAGRAM_MAX_SIZE = 1500;
    public static  final long PACKET_DATA_MAX_SIZE = 1450; // 1500 - 50

    public static enum COMMAND_TYPE {

        INVALID_COMMAND((byte)0),
        SEARCH_ENGINE_REQUEST((byte)1),
        SONG_MP3_PACKETS_RANGE_REQUEST((byte)3),
        SONG_MP3_N_PACKETS_REQUEST((byte)4);
        
        private COMMAND_TYPE(byte value) {
            this.value = value;
        }
        
        public final byte value;
        
        public static COMMAND_TYPE fromByte(byte value) {
            if (value == SEARCH_ENGINE_REQUEST.value) return SEARCH_ENGINE_REQUEST;
            // else if (value == SONG_MP3_REQUEST.value) return SONG_MP3_REQUEST;
            else if (value == SONG_MP3_PACKETS_RANGE_REQUEST.value) return SONG_MP3_PACKETS_RANGE_REQUEST;
            else if (value == SONG_MP3_N_PACKETS_REQUEST.value) return SONG_MP3_N_PACKETS_REQUEST;
            else return INVALID_COMMAND;
            
        }
    }

    // ##################### nested classes #####################
    /*
     * The following classes are input/output data types for the different request/receive methods
     */

    // ##################### SearchEngine #####################

    public static class ResponseSearchEngine_t {
        public long cookie;                 // 8 bytes  
        public ArrayList<Song> songList;    // rest of bytes (10 songs)

        public ResponseSearchEngine_t(long cookie, ArrayList<Song> songList) {
            this.songList = songList;
            this.cookie = cookie;
        }

        public ResponseSearchEngine_t() {

        }
    }

    // ##################### MP3 #####################
 
}
