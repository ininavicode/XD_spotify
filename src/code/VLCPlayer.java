package code;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class VLCPlayer {
    // ##################### CONSTANTS #####################
    private final String VLC_LINUX_DIR = "/usr/bin/vlc";

    // ##################### INSTANCE ATTRIBUTES #####################
    ProcessBuilder vlcProcessBuilder; // The instance that will have the process builder.
    Process vlcProcess;               // The instance that will have the process playing.
    OutputStream vlcCommandCons;      // The instance that will permit the manipulation of the VLC reproducer.
    PrintWriter vlcCommandWriter;     // The instance that will manipulate the console operations.
    BufferedReader vlcResponse;          // The instance that will hold the responses of the VLC via terminal.
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
            vlcResponse = vlcProcess.inputReader();
            vlcCommandWriter = new PrintWriter(vlcCommandCons, false); // Autoflush to true, for each command, flush the stream for future input.
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
        }
    }

    /**
     * Method that resumes the current song.
     */
    public void resume() {
        if(!actuallyPlaying) {
            vlcCommandWriter.println("play");
            actuallyPlaying = true;
        }
    }
    
    /**
     * Method that allows the user to go to a position in time of the song playing.
     * 
     * @param sec The seconds of the time the song is wanted to be reproduced.
     */
    public void goToInSeconds(int sec) {
        vlcCommandWriter.println("seek " + sec);
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
            Thread.sleep(1000);
            System.out.println(vlcResponse.readLine());
            System.out.println(vlcResponse.readLine());
            String temp = vlcResponse.readLine();
            numSec = Integer.parseInt(temp.substring(temp.indexOf('>') + 2));
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
            vlcCommandWriter.println("get_time");
            System.out.println(vlcResponse);
            vlcResponse.readLine();
            vlcResponse.readLine();
            System.out.println(vlcResponse);
            String temp = vlcResponse.readLine();
            numSec = Integer.parseInt(temp.substring(temp.indexOf('>') + 2));
        }
        catch(IOException exc) {
            exc.printStackTrace();
        }
        return numSec;
    }

    /**
     * Method that advances the song in 10 seconds.
     */
    public void advance10Seconds() {
        vlcCommandWriter.println("seek +10");
    }

    /**
     * Method that make the song go back by 10 seconds.
     */
    public void goBack10Seconds() {
        vlcCommandWriter.println("seek -10");
    }
}
