package main;

import code.*;

public class testVLCJPlayer {

    static VLCJPlayer player = new VLCJPlayer();
    private static final Object lock = new Object();
    private static boolean isPaused = false;

    private static void updateTime() {
        while (true) {
            synchronized (lock) {
                while (isPaused) {
                    try {
                        lock.wait(); // Wait if the thread is paused
                    } catch (InterruptedException e) {
                        System.out.println("Update time thread interrupted.");
                        return; // Exit the loop if the thread is interrupted
                    }
                }

                // If not paused, update the time
                System.out.print("\n" + player.getActualTime());
            }

            try {
                Thread.sleep(1000); // Adjust the time as needed
            } catch (InterruptedException e) {
                System.out.println("Update time thread interrupted.");
                return; // Exit the loop if the thread is interrupted
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Thread updateTimeThread = new Thread(testVLCJPlayer::updateTime);
        updateTimeThread.start();

        player.play("data/ISABELLA.mp3");
        System.out.println("TOTAL LENGTH OF RESOURCE: " + player.getTotalLength());

        while (true) {
            int option = System.in.read();

            switch (option) {
                case 'p' -> {
                    synchronized (lock) {
                        isPaused = true; // Set the pause flag
                        player.pause();
                    }
                }
                case 's' -> {
                    synchronized (lock) {
                        isPaused = false; // Clear the pause flag
                        lock.notify(); // Notify the waiting thread to resume
                        player.play();
                    }
                }
                case 'a' -> player.advance10Seconds();
                case 'r' -> player.goBack10Seconds();
            }
        }
    }
}
