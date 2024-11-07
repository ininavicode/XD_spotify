package song;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class SongList {

    /*
     * Converts the data of the list to a String instance.
     */
    public static String toString(ArrayList<Song> songsList) {
        String buffer = "";
        final int songListSize = songsList.size();

        for (int i = 0; i < songListSize; i++) {
            buffer += songsList.get(i).toCharRaw() + (i < (songListSize - 1) ? "\n" : "");
        }
        return buffer;
    }

    /**
     * Converts the data of an ArrayList<Song> instance to a byte[]
     */
    static public byte[] toByteRaw(ArrayList<Song> songsList) {
        return toString(songsList).getBytes();
    } 

    /**
     * @param raw : byte[] The data of the ArrayList<Song> encoded with the default charset 
     * @return An ArrayList<Song> instance containing the data encoded with the default charset at byte[] raw
     */
    static public ArrayList<Song> fromByteRaw(byte[] raw) throws IllegalArgumentException {
        String decodedRaw = new String(raw);
        String[] songRawList = decodedRaw.split("\n");
        ArrayList<Song> output;

        // this indicates that there is any song at the list
        if (songRawList.length == 1 &&  songRawList[0].length() == 0) {
            output = new ArrayList<>(0);
        }
        else {
            
            output = new ArrayList<>(songRawList.length);
            
            // generate a song for each string and add it into the songs list
            for (String songRaw : songRawList) {
                
                output.add(new Song(songRaw));
            }
        }

        
        return output;
    }

    /**
     * @return  If the file is empty (0 songs), returns an empty ArrayList<Song>(1)
     *          If the file contains data, returns an ArrayList<Song> with the proper data
     * @throws FileNotFoundException If the file does not exist
     */
    public static ArrayList<Song> fromFile(String filename) throws FileNotFoundException {

        File file = new File(filename);

        if (!file.exists()) {
            throw new FileNotFoundException("The file " + filename + " does not exist");
        }
        
        Scanner scanner = new Scanner(file);
        
        // obtain the number of songs at the file
        String parsed = scanner.nextLine();
        int nSongs = Integer.parseInt(parsed);

        if (nSongs > 0) {
            ArrayList<Song> output = new ArrayList<>(nSongs);
    
            for (int i = 0; i < nSongs; i++) {
                // add a new song with the data of the next line of the file
                output.add(new Song(scanner.nextLine()));
            }

            scanner.close();
            return output;
        }
        else {
            scanner.close();
            return new ArrayList<>(1);
        }

        
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
