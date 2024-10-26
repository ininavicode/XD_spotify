package song;

import java.util.ArrayList;

public class SongList {
    /**
     * Converts the data of an ArrayList<Song> instance to a byte[]
     */
    static public byte[] toByteRaw(ArrayList<Song> songsList) {
        String buffer = "";
        final int songListSize = songsList.size();

        for (int i = 0; i < songListSize; i++) {
            buffer += songsList.get(i).toCharRaw() + (i < (songListSize - 1) ? "\n" : "");
        }

        return buffer.getBytes();
    } 

    /**
     * @param raw : byte[] The data of the ArrayList<Song> encoded with the default charset 
     * @return An ArrayList<Song> instance containing the data encoded with the default charset at byte[] raw
     */
    static public ArrayList<Song> fromByteRaw(byte[] raw) throws ExceptionInInitializerError {
        String decodedRaw = new String(raw);
        String[] songRawList = decodedRaw.split("\n");
        
        ArrayList<Song> output = new ArrayList<>(songRawList.length);
        
        // generate a song for each string and add it into the songs list
        for (String songRaw : songRawList) {
            output.add(new Song(songRaw));
        }
        
        return output;
    }

    /**
     * @return True if s1(i) = s2(2) for each i, and s1.size = s2.size
     */
    static public boolean equals(ArrayList<Song> s1, ArrayList<Song> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }

        for (int i = 0; i < s1.size(); i++) {
            if (!s1.get(i).equals(s2.get(i))) {
                return false;
            }
        }

        return true;
    }

}
