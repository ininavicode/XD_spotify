package main;

import code.*;

public class testVLCJPlayer {

    static VLCJPlayer player = new VLCJPlayer();

    private static void updateTime() {
        long lastTime = player.getActualTime();

        while (true) {
            if (lastTime != player.getActualTime()) {
                System.out.print("\n" + player.getActualTime());
                lastTime = player.getActualTime();
            }
            try {
                // Add a short sleep to avoid busy-waiting and allow for thread interruptions
                Thread.sleep(1000); // Adjust the time as needed
            } catch (InterruptedException e) {
                System.out.println("Update time thread interrupted.");
                return; // Exit the loop if the thread is interrupted
            }
        }
    }
    public static void main(String[] args) throws Exception {

        // Start the updateTime method in a new thread
        Thread updateTimeThread = new Thread(testVLCJPlayer::updateTime);
        updateTimeThread.start();

        player.play("data/ISABELLA.mp3");
        System.out.println("TOTAL LENGTH OF RESOURCE: " + player.getTotalLength());

        while (true) { 
            int option = System.in.read();
    
            switch (option) {
                case 'p' -> player.pause();
                
                case 's' -> player.play();

                case 'a' -> player.advance10Seconds();

                case 'r' -> player.goBack10Seconds();
            }

            
        }
    }
}
