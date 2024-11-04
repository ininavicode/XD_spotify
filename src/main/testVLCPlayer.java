package main;

import code.VLCPlayer;

public class testVLCPlayer {
    public static void main(String[] args) throws Exception {
        VLCPlayer vlcPlayer = new VLCPlayer("data/ISABELLA.mp3");
        System.out.println("File charged");
        Thread.sleep(5000);
        System.out.println(vlcPlayer.getActualTime());
        System.out.println(vlcPlayer.getTotalTime());
        System.out.println("PAUSING");
        vlcPlayer.pause();
        Thread.sleep(3000);
        System.out.println("RESUMING");
        vlcPlayer.resume();
        System.out.println(vlcPlayer.getTotalTime());
        Thread.sleep(3000);
        System.out.println("Going back 10 seconds");
        vlcPlayer.goBack10Seconds();
        Thread.sleep(3000);
        System.out.println("Going forward 10 seconds");
        vlcPlayer.advance10Seconds();
        Thread.sleep(10000);
    }
}
