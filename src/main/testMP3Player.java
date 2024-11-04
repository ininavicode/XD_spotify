package main;

import code.*;

public class testMP3Player {
    public static void main(String args[]) throws InterruptedException {
        System.out.println("Initialising incorrectly...");
        MP3Player rep = new MP3Player("data/non_existent.mp3");
        System.out.println("Setting a new MP3 file.");
        rep.newMP3File("data/ISABELLA.mp3");       
        System.out.println("Playing for 5 seconds...");
        rep.play();
        Thread.sleep(5000);
        System.out.println("Pausing for 5 seconds...");
        rep.pause();
        Thread.sleep(5000);
        System.out.println("Resuming for 6 seconds...");
        rep.play();
        Thread.sleep(6000);
        System.out.println("Changing song...");
        rep.newMP3File("data/Family Business.mp3");      
        System.out.println("Playing for 5 seconds...");
        Thread.sleep(5000);
        System.out.println("Changing to a non-existent song...");
        rep.newMP3File("data/non_existent.mp3");
        System.out.println("Pausing, then changing and then resuming...");
        rep.pause();
        rep.newMP3File("data/ISABELLA.mp3");
        rep.play();
        System.out.println("Playing the song indefinetely...");
    }
}
