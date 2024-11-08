package search_engine;

import java.util.ArrayList;
import java.util.HashMap;
import song.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class that defines the search engine for the server and the client.
 * It has a hash for songs and an arraylist for saving the info of the songs.
 */

public class SearchEngine {
    // Instance attributes
    private HashMap<Song, String> songsHash; 
    String fileName;

    /**
     * Constructor of the class.
     * 
     * @param fileName The file from which the search engine will initially load all the data.
     * @pre The filename must be in the correct format, with the list of songs correctly formatted.
     */
    public SearchEngine(String fileName) {
        songsHash = new HashMap<>();
        try(Scanner fileReader = new Scanner(new File(fileName))) {
            Song nextSong;
            while(fileReader.hasNextLine()) {
                nextSong = new Song(fileReader.nextLine());
                songsHash.put(nextSong, Song.songToFilename(nextSong));
            }
            this.fileName = fileName;
        }
        catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Getter of a song by its name.
     * 
     * @param song The name of the song.
     * @return Return the MP3 name of the song, null if it is not listed.
     */
    public String getMP3ByName(Song song) {
        return songsHash.get(song);
    }

    /**
     * Returns a copy of the list of songs.
     * 
     * @return List of the songs.
     */
    public ArrayList<Song> getSongsList() {
        return new ArrayList<>(songsHash.keySet());
    }

    /**
     * Indicates if the hash contains the mp3.
     * 
     * @param mp3Name The MP3 name to be checked.
     * @return True if it is contained, false otherwise.
     */
    public boolean containsMP3(String mp3Name) {
        return songsHash.containsValue(mp3Name);
    }

    /**
     * Returns all the songs that have a coincidence with cond.
     * 
     * @param cond The string that conditions the result.
     * @return The sublist of songs that contain cond. If none matches cond, an empty list.
     * @pre cond != null.
     */
    public ArrayList<Song> getSongsWithCondition(String cond) {
        ArrayList<Song> newList = new ArrayList<>();
        String updatedCond = cond.toLowerCase();                // To ignore the upper or lower case with the searchs.
        songsHash.forEach((key, value) -> {
                if(key.getName().toLowerCase().startsWith(updatedCond))
                    newList.add(key);
            }
        );
        return newList;
    }

    // TODO: EDIT FILE WITH NEW SONG ADDITIONS.
    /**
     * Adds a song to the list and the hash of songs.
     * 
     * @param song The song which will be added.
     * @param mp3dir The name of the mp3.
     */
    public void addSong(Song song, String mp3dir) {
        if(song != null && mp3dir != null) {
            songsHash.put(song, mp3dir);
            try(FileWriter writer = new FileWriter(new File(fileName), true)) {
                writer.write(song.toCharRaw() + '\n'); // Append the new song to the end of the file.
            }
            catch (IOException exc) {
                songsHash.remove(song);
                exc.printStackTrace();
            }
        }
    }

    // For now this option is not available.
    // public void removeSong(Song song) {
    //     if(song != null) songsHash.remove(song);
    // }

    // ##################### CLASS METHODS #####################
    /**
     * Class method that allows to search in a subList for the coincidences to cond.
     * 
     * @param subList The sublist from which the search will be executed.
     * @param cond The string that holds the caracter 
     * @return The sublist of songs that start with cond. If none matches cond, an empty list.
     * @pre subList != null && cond != null.
     */
    public static ArrayList<Song> getSongsWithCondition(ArrayList<Song> subList, String cond) {
        ArrayList<Song> newList = new ArrayList<>();
        String updatedCond = cond.toLowerCase();                // To ignore the upper or lower case with the searchs.
        for(Song elem : subList) {
            if(elem.getName().toLowerCase().startsWith(updatedCond))
                newList.add(elem);
        }
        return newList;
    }
}
