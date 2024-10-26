import java.util.ArrayList;
import java.util.Arrays;


class Song {
    // ##################### properties #####################
    private String name;
    private ArrayList<String> authorsList;


    // ##################### constructor #####################
    public Song(String name, String ... authors) {
        this.name = name;

        // as List is a subinterface of Collection, the argument accepts a list instead of a Collection too
        authorsList = new ArrayList<>(Arrays.asList(authors)); 
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
     * @Return: The data of the instance converted to the format of the client-server communication
     */
    public String toRaw() {
        String buffer = name;
        for (String author : authorsList) {
            buffer += ":" + author;
        }

        return buffer;
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
    
}