package code;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

public class VLCPlayer {
    // ##################### CONSTANTS #####################
    private final String VLC_LINUX_DIR = "/usr/bin/vlc";

    // ##################### INSTANCE ATTRIBUTES #####################
    ProcessBuilder vlcProcessBuilder; // The instance that will have the process builder.
    Process vlcProcess;               // The instance that will have the process playing.
    OutputStream vlcCommandCons;      // The instance that will permit the manipulation of the VLC reproducer.
    PrintWriter vlcCommandWriter;     // The instance that will manipulate the console operations.
    InputStream vlcResponse;          // The instance that will hold the responses of the VLC via terminal.
    boolean actuallyPlaying;          // The instance that controls whether the music is being or not being played.
                                      // There would be no problems if pause() is called when the song is pauses, it will continue its reproduction,
                                      // but to make things cohesive and understandable, pause() method will only pause the song.

    // ##################### CONSTRUCTOR OF THE CLASS #####################
    /**
     * Constructor of the class
     * 
     * @param fileName The pathname of the MP3.
     */
    public VLCPlayer(String fileName) {
        vlcProcessBuilder = new ProcessBuilder(VLC_LINUX_DIR, fileName, "--intf", "rc", "--play-and-exit"); // Interface remote control.
        try {
            vlcProcess = vlcProcessBuilder.start();
            actuallyPlaying = true;
            vlcCommandCons = vlcProcess.getOutputStream();
            vlcResponse = vlcProcess.getInputStream();
            flush();
            vlcCommandWriter = new PrintWriter(vlcCommandCons, true); // Autoflush to true, for each command, flush the stream for future input.
        } 
        catch(IOException exc) {
           exc.printStackTrace();
        }
    }

    // ##################### INSTANCE METHODS #####################
    /**
     * Method that pauses the current song.
     */
    public void pause() {
        if(actuallyPlaying) {
            vlcCommandWriter.println("pause");
            actuallyPlaying = false;
            flush();
        }
    }

    /**
     * Method that resumes the current song.
     */
    public void resume() {
        if(!actuallyPlaying) {
            vlcCommandWriter.println("play");
            actuallyPlaying = true;
            flush();
        }
    }
    
    /**
     * Method that allows the user to go to a position in time of the song playing.
     * 
     * @param sec The seconds of the time the song is wanted to be reproduced.
     */
    public void goToInSeconds(int sec) {
        vlcCommandWriter.println("seek " + sec);
        flush();
    }

    /**
     * Method that gets the actual time of reproduction in seconds.
     * 
     * @return The actual time of reproduction in seconds.
     */
    public int getActualTime() {
        int numSec = -1;
        try {
            vlcCommandWriter.println("get_time");
            Thread.sleep(10); // wait for the response in the buffer to be loaded.
            int available = vlcResponse.available();
            byte[] info = new byte[available];
            vlcResponse.read(info, 0, available);
            String temp = new String(info);
            numSec = Integer.parseInt(temp.substring(0, temp.lastIndexOf('>') - 1).trim()); // +2 to eliminate "> " and -1 to eliminate "\n".
            flush();
        }
        catch(IOException | InterruptedException exc) {
            exc.printStackTrace();
        }
        return numSec;
    }
    
    /**
     * Method that returns the number of seconds a MP3 lasts.
     * 
     * @return The number of seconds.
     */
    public int getTotalTime() {
        int numSec = -1;
        try {
            vlcCommandWriter.println("get_length");
            Thread.sleep(10); // wait for the response in the buffer to be loaded.
            int available = vlcResponse.available();
            byte[] info = new byte[available];
            vlcResponse.read(info, 0, available);
            String temp = new String(info);
            numSec = Integer.parseInt(temp.substring(0, temp.lastIndexOf('>') - 1).trim()); // +2 to eliminate "> " and -1 to eliminate "\n".
            flush();
        }
        catch(IOException | InterruptedException exc) {
            exc.printStackTrace();
        }
        return numSec;
    }

    /**
     * Method that advances the song in 10 seconds.
     */
    public void advance10Seconds() {
        goToInSeconds(getActualTime() + 10);
    }

    /**
     * Method that make the song go back by 10 seconds.
     */
    public void goBack10Seconds() {
        goToInSeconds(getActualTime() - 10);
    }

    /**
     * Method that flushed the entry.
     */
    private void flush() {
        try {
            Thread.sleep(10); // Take some time for the buffer to be completely charged.
            while(vlcResponse.available() > 0) { // While it has data ...
                vlcResponse.read();              // ... read the bytes.
            }
        }
        catch(IOException | InterruptedException exc) {
            exc.printStackTrace();
        }
    }
}
