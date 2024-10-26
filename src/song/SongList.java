package song;

import java.util.ArrayList;

public class SongList {
    static public String toRaw(ArrayList<Song> songsList) {
        String buffer = "";
        final int songListSize = songsList.size();

        for (int i = 0; i < songListSize; i++) {
            buffer += songsList.get(i).toRaw() + (i < (songListSize - 1) ? "\n" : "");
        }

        return buffer;
    } 

    static public ArrayList<Song> fromRaw(String raw) throws ExceptionInInitializerError {
        String[] songRawList = raw.split("\n");

        ArrayList<Song> output = new ArrayList<>(songRawList.length);

        // generate a song for each string and add it into the songs list
        for (String songRaw : songRawList) {
            output.add(new Song(songRaw));
        }
        
        return output;
    }

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
