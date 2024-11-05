package code;

import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

public class VLCJPlayer {
    // ##################### INSTANCE ATTRIBUTES #####################
    private AudioPlayerComponent player;

    // ##################### CONSTRUCTOR #####################
    /**
     * Constructor of the class.
     */
    public VLCJPlayer() {
        player = new AudioPlayerComponent();
    }

    // // ##################### ALL THE METHODS #####################
    /**
     * Method that charges some media and plays it in a separate Thread.
     * 
     * @param fileName The name of the file to play.
     */
    public void play(String fileName) {
        player.mediaPlayer().media().play(fileName);
    }

    /**
     * Method that plays the media that is actually on the player.
     * 
     */
    public void play() {
        player.mediaPlayer().controls().play();
    }

    /**
     * Method that pauses the media actually being played.
     */
    public void pause() {
        player.mediaPlayer().controls().pause();
    }

    /**
     * Method to go to a specific time in the song.
     * 
     * @param seconds The time in seconds to go to.
     */
    public void goTo(long seconds) {
        player.mediaPlayer().controls().setTime(seconds * 1000);
    }

    /**
     * Method to advance 10 seconds the song.
     */
    public void advance10Seconds() {
        player.mediaPlayer().controls().skipTime(10000);;
    }

    /**
     * Method to go back 10 seconds of the song.
     */
    public void goBack10Seconds() {
        player.mediaPlayer().controls().skipPosition(-1000);
    }

    /**
     * Method that prepares some media to be played.
     * 
     * @param fileName The media to be played.
     */
    public void chargeFile(String fileName) {
        player.mediaPlayer().media().prepare(fileName);
    }

    /**
     * Gets the duration of the media charged in seconds.
     * 
     * @return The duration of the media charged in seconds.
     */
    public long getTotalLength() {
        return player.mediaPlayer().status().length() / 1000;
    }

    /**
     * Return the actual time of the player.
     * 
     * @return The actual time of the file, in seconds.
     */
    public long getActualTime() {
        return player.mediaPlayer().status().time() / 1000;
    }
}
