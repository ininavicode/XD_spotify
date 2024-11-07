package song;

// libs (start) +++++++++++++

import java.util.ArrayList;
import java.util.Arrays;
// libs (end)   -------------


public class Song {
    // ##################### properties #####################
    private String name;
    private ArrayList<String> authorsList;


    // ##################### constructor #####################
    /**
     * Initializes the data of the instance with the given song name and authors
     */
    public Song(String name, String ... authors) throws ExceptionInInitializerError {
        if (name == null || authors == null) {
            throw new ExceptionInInitializerError();
        }
        
        this.name = name;

        // as List is a subinterface of Collection, the argument accepts a list instead of a Collection too
        authorsList = new ArrayList<>(Arrays.asList(authors)); 
    }

    /**
     * Initializes the instance with the data of the rawChar, that should be in the proper format
     */
    public Song(String rawChar) throws IllegalArgumentException {
            setFromCharRaw(rawChar);
    }

    /**
     * Initializes the instance with the data of the raw byte, that should be in the proper format
     */
    public Song(byte[] raw) throws IllegalArgumentException {
        setFromByteRaw(raw);
            
    }
    
    // ##################### methods #####################


    /**
     * Adds a new author if it does not exist yet
     * @Param author : Name of the author to add
     */
    public void addAuthor(String author) {
        if (!authorsList.contains(author)) {
            authorsList.add(author);
        }
    }

    /**
     * Removes the first ocurrence of author at the list
     * @Return: True if any author was deleted. False else
     */
    public boolean removeAuthor(String author) {
        return authorsList.remove(author);
    }

    /**
     * 
     * @Return: The data of the instance to a byte raw
     */
    public byte[] toByteRaw() {
        
        return toCharRaw().getBytes();
    }

    /**
     * This function returns the decoded byteRaw that should return toByteRaw method
     * @return The string with the decoded data
     */
    public String toCharRaw() {
        String buffer = name;
        for (String author : authorsList) {
            buffer += ";" + author;
        }

        return buffer;
    }

    /**
     * Overwrites the data of this instance with the data of the given raw
     * @param raw : Formatted string encoded with default charset with the data of the song and author(s)
     * @throws InvalidPropertiesFormatException: If the format of the raw is invalid
     */
    public void setFromCharRaw(String raw) throws IllegalArgumentException {
        String[] parsedList = raw.split(";");

        authorsList = new ArrayList<>(parsedList.length);

        if (parsedList.length < 2) {
            throw new IllegalArgumentException();
        }
        else {
            
            name = parsedList[0];
    
            for (int i = 1 ; i < parsedList.length; i++) {
                authorsList.add(i - 1, parsedList[i]);
            }
        }

    }

    /**
     * Overwrites the data of this instance with the data of the given raw
     * @param raw : Formatted string with the data of the song and author(s)
     * @throws InvalidPropertiesFormatException: If the format of the raw is invalid
     */
    public void setFromByteRaw(byte[] raw) throws IllegalArgumentException {
        String decodedRaw = new String(raw);
        setFromCharRaw(decodedRaw);
    }

    @Override
    public Song clone() throws CloneNotSupportedException {
        return new Song(name, (String[])authorsList.toArray());
    }

    @Override
    public String toString() {
        String buffer = "Song@" + Integer.toHexString(hashCode());
        buffer += "\n\tName: " + name;

        buffer += "\n\tAuthors: " + authorsList.get(0); 

        for (int i = 1; i < authorsList.size(); i++) {
            buffer += ", " + authorsList.get(i);
        }

        return buffer;
    }

    /**
     * Compares if the data of 2 songs is exactly the same for the name and the authors.
     * @return  If two songs have the same authors but in different order, returns false;
     *          If the name and authors match returns true;
     */
    public boolean equals(Song other) {
        if (name.compareTo(other.name) != 0) {
            return false;
        }

        for (int i = 0; i < authorsList.size(); i++) {
            if (authorsList.get(i).compareTo(other.authorsList.get(i)) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts the name of the song to a the established name for the .mp3 files
     * @return The name of the .mp3
     */
    public String toFilename() {
        return songToFilename(this);
    }

    // ##################### getters #####################
    /**
     * @Return The author at the index i
     * @Throws: IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
     */
    public String getAuthor(int i) throws IndexOutOfBoundsException {

        return authorsList.get(i);
    }

    /**
     * @Return The size of the ArrayList of authors
     */
    public int getAuthorsCount() {
        return authorsList.size();
    }

    /**
     * @Return: The name of the song
     */
    public String getName() {
        return name;
    }

    // ##################### static #####################
    /**
     * Converts the name of the song to a the established name for the .mp3 files
     * 
     * @return The name of the .mp3
     */
    public static String songToFilename(Song song) {
        String aux = song.getName().replace(' ', '-');
        int numAuthors = song.getAuthorsCount();
        for(int i = 0; i < numAuthors; i++)  {
            aux += "_" + song.getAuthor(i).replace(' ', '-');
        }
        return aux + ".mp3";
    }

    /**
     * Method that converts a filename to a song instance. The format 
     * of the song must be
     *          song-name_author1_author2_..._author-n.mp3
     * 
     * @param fileName The name of the file that represents the song.
     * @return The instance of the song created.
     * @pre The file name must be in the correct format especified above.
     */
    public static Song filenameToSong(String fileName) {
        String[] result = fileName.split("_");
        Song resultSong = new Song(result[0]);
        for(int i = 1; i < result.length; i++) {
            resultSong.addAuthor(result[i]);
        }
        return resultSong;
    }

    /**
     * Converts the name of the song (in string format) to a the established name for the .mp3 files
     * @return The name of the .mp3
     */
    public static String songStringToFilename(String name) {
        return songToFilename(new Song(name));
    }
    
}