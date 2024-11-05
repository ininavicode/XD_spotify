package main;

import code.*;

public class testVLCJPlayer {
    public static void main(String[] args) throws Exception {
        VLCJPlayer player = new VLCJPlayer();
        player.play("data/ISABELLA.mp3");
        System.out.println("TOTAL LENGTH OF RESOURCE: " + player.getTotalLength());
        for(;;) {
            System.out.println(player.getActualTime());
        }
    }
}
