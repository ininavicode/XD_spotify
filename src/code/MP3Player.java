package code;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MP3Player {
    // ##################### INSTANCE ATTRIBUTES #####################
    private Player mp3Player;
    private FileInputStream file;
    private Thread reproducerThread;
    private Object playPauseHandler; // We will have methods that reproduce and others that pause/resume. To avoid bad syncro, it is needed an object to do so.
    /**
     * To control the actual state of the player.
     */
    private boolean actuallyPlaying;
    
    /**
     * To control whether the file has been correctly charged and the attributes as well.
     */
    private boolean fileCharged;
    
    /**
     * To control whether the song has started or not, to restart the reproducer thread execution.
     */
    private boolean notStarted;

    /**
     * Constructor of the class. 
     * 
     * @param fileName File of the first song to play.
     */
    public MP3Player(String fileName) {
        // Initialisation of the thread and the synchronisation handler, both of which are always necessary.
        reproducerThread = new Thread(() -> { 
                playInternal();
        });    
        playPauseHandler = new Object();
        try {
            file = new FileInputStream(fileName);
            mp3Player = new Player(file);
            actuallyPlaying = false; // Initially paused.
            fileCharged = true;
            notStarted = true;
        }
        catch (FileNotFoundException | JavaLayerException exc) {
            System.err.println("##################### ERROR #####################\nFile not found, instance not initialised\n#################################################");
            actuallyPlaying = false; // Initially paused.
            fileCharged = false;
            notStarted = true;
        }
    }

    // ##################### GETTERS #####################

    /**
     * Methods that returns the actual time of the active track.
     * 
     * @return The time in seconds.
     */
    public double getActualTime() {
        synchronized(playPauseHandler){
            return(mp3Player.getPosition()/1000); // As the reproducer thread may be changing the state with playing, we must be syncronised.
        }
    }

    // ##################### SETTERS #####################
    /**
     * Method to play the file in a thread (that will be stored in reproducerThread).
     */
    public void play() {
        if(fileCharged) {
            if(notStarted) {
                notStarted = false;
                actuallyPlaying = true; // Starting the playing.
                reproducerThread.start(); // Starting the execution of the thread.
            }
            else if (!actuallyPlaying){
                synchronized(playPauseHandler) {
                    actuallyPlaying = true;
                    playPauseHandler.notifyAll();
                }
            }
        }
    }

    /**
     * Method to pause the track.
     */
    public void pause() {
        synchronized (playPauseHandler) {
            if(fileCharged && actuallyPlaying)
                actuallyPlaying = false;
        }
    }

    /**
     * Method that allows the change to another MP3 file for its reproduction.
     * 
     * @param fileName The new path for the new MP3 file to load.
     */
    public void newMP3File(String fileName) {
        synchronized(playPauseHandler) { // As the songs can be playing when the change occurs, it has to be a syncronised change.
            try {
                file = new FileInputStream(fileName);
                mp3Player = new Player(file);
                fileCharged = true;
                if(!actuallyPlaying) play(); // If the file was paused, resume playing of the file.
            }
            catch (FileNotFoundException | JavaLayerException exc) {
                System.err.println("##################### ERROR #####################\nFile not found, song not changed\n#################################################");
                // File changed, not started and actually playing will remain as they were intially.
            }
        }
    }

    /**
     * Method that is run by a separated thread to execute the playing of a song frame by frame.
     */
    private void playInternal() {
        boolean songFinished = false;
        while(!songFinished) {
            try {
                songFinished = !mp3Player.play(1); // Play a frame and evaluate its end.
            }
            catch (JavaLayerException exc) {
                songFinished = true; // Error encountered, stop reproduction.
            }
            synchronized(playPauseHandler) {
                if(!actuallyPlaying)         // If the song is paused ...
                    try{
                        playPauseHandler.wait(); // put this thread (reproducerThread) in waiting mode.
                        // Reanudation occurs when resume method notifies all the threads, including this one, to continue their reproduction.
                    }
                    catch(InterruptedException exc) {
                        break; // end of player.
                    }
            }
        }
        // SONG FINISHED.
        synchronized(playPauseHandler) {
            mp3Player.close(); // Close the song.
            fileCharged = false;
            notStarted = true;
            actuallyPlaying = false;
        }
    }
}
