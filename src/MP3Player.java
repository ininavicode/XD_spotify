import javafx.scene.media.*;
import javafx.util.Duration;
import java.io.File;

// TODO: Add exceptions for when a media cannot be loaded.

public class MP3Player {
    // Instance attributes
    private Media songURI;
    private MediaPlayer reproducer;

    /**
     * Constructor of the class, firstly an audio resource should be located.
     * 
     * @param resourceLocation The name of the resource. It must be located in the directory
     * of the java runtime process.
     */
    public MP3Player(String resourceLocation) {
        try {
            songURI = new Media(new File(resourceLocation).toURI().toString());
            reproducer = new MediaPlayer(songURI);
        }
        // Two catches blocks for correct display of errors.
        catch (NullPointerException exc) {
            exc.printStackTrace();
            System.err.println("Not valid resource to initialise.");
        }
        catch (IllegalArgumentException exc) {
            exc.printStackTrace();
            System.err.println("\nNo correct URI, refer to RFC-2396 for correct URIs, or non-existent resource specified.");
        }
    }

    /* GETTERS */

    /**
     * Methods that returns the actual time of the active track.
     * 
     * @return The time in seconds.
     */
    public double getActualTime() {
        return(reproducer.getCurrentTime().toSeconds());
    }

    /**
     * Method that returns the total time of the active track.
     * 
     * @return The time in seconds.
     */
    public double getTotalTime() {
        return songURI.getDuration().toSeconds();
    }

    /**
     * Returns the actual volume, [0, 100].
     * 
     * @return The actual volume of the track.
     */
    public double getActualVolume() {
        return reproducer.getVolume()*100;
    }

    /* SETTERS */

    /**
     * Change the actual track time.
     * 
     * @param seconds The second of the moment the track is desired to be.
     */
    public void setTrackTime(double seconds) {
        reproducer.seek(Duration.seconds(seconds)); // Note: All the possible comprovations done within the method seek.
        // TODO: Test negative seconds.
    }
    
    /**
     * Method to set a new track to be played.
     * 
     * @param resourceLocation The name of the resource. It must be located in the directory
     * of the java runtime process.
     */
    public void setNewTrack(String resourceLocation) {
        try {
            songURI = new Media(new File(resourceLocation).toURI().toString());
            reproducer = new MediaPlayer(songURI);
        }
        // Two catches blocks for correct display of errors.
        catch (NullPointerException exc) {
            exc.printStackTrace();
            System.err.println("Not valid resource to initialise.");
        }
        catch (IllegalArgumentException exc) {
            exc.printStackTrace();
            System.err.println("\nNo correct URI, refer to RFC-2396 for correct URIs, or non-existent resource specified.");
        }
    }

    /**
     * Method to set the volume to the one specified.
     * 
     * @param volume The new volume, [0, 100].
     */
    public void setVolume(double volume) {
        reproducer.setVolume(volume / 100);
    }

    /* 
     * MANIPULATORS: These are methods thought to be used by the user of the player directly. 
     * This methods would normally not be included, but for the comfort of the project they will be useful.
     */
    /**
     * Method that plays the media.
     */
    public void play() {
        reproducer.play();
    }

    /**
     * Method that pauses the media.
     */
    public void pause() {
        reproducer.pause();
    }

    /**
     * Method that advances the reproduction to + 10 seconds.
     */
    public void advance10Seconds() {
        this.setTrackTime(this.getActualTime() + 10);
    }

    /**
     * Method that goes backwards 10 seconds.
     */
    public void backUp10Seconds() {
        this.setTrackTime(this.getActualTime() - 10);
    }

    /**
     * Method to turn up the volume in 10 units.
     */
    public void turnUpVolume() {
        this.setVolume(getActualVolume() + 10);
    }

    /**
     * Method to turn down the volume in 10 units.
     */
    public void turnDownVolume() {
        this.setVolume(getActualVolume() - 10);
    }
}