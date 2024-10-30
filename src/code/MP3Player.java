package code;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MP3Player {
    private Player mp3Player;
    private FileInputStream file;

    /**
     * Constructor of the class. 
     * 
     * @param fileName File of the first song to play.
     */
    public MP3Player(String fileName) {
        try {
            file = new FileInputStream(fileName);
            mp3Player = new Player(file);
        }
        catch (FileNotFoundException | JavaLayerException exc) {
            System.err.println("File not found, instance not initialised");
        }
    }

    /* GETTERS */
    /* GETTERS */

    /**
     * Methods that returns the actual time of the active track.
     * 
     * @return The time in seconds.
     */
    public double getActualTime() {
        return(mp3Player.getPosition()*1000);
    }

    // TODO: Find an equivalent.
    // /**
    //  * Method that returns the total time of the active track.
    //  * 
    //  * @return The time in seconds.
    //  */
    // public double getTotalTime() {
    //     return songURI.getDuration().toSeconds();
    // }

    /**
     * Method to play the file.
     */
    public void play() {
        
        new Thread(() -> {
            try {
                mp3Player.play();
            } catch (Exception e) {
                System.err.println("Couldn't play the track.");
            }
        }).start();
    }
}
