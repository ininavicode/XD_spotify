package main;
import code.*;

public class testMP3Player {
    public static void main(String args[]) {
        System.out.println("EXECUTED");
        MP3Player rep = new MP3Player("/home/debian/DatosUNI/Xarxes De Dades/XD_spotify/data/ISABELLA.mp3");
        rep.play();
        System.out.println("HI"); // Proof to show that the execution continues.
        // String stop = teclat.nextLine();
        // System.out.println(stop.equals("s"));
    }
    
}
