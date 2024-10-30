package main;
import code.*;

public class testMP3Player {
    public static void main(String args[]) {
        System.out.println("EXECUTED");
        MP3Player rep = new MP3Player("/mnt/e/MUSICA-SPOTY/ISABELLA.mp3");
        rep.play();

        // Esperar un tiempo antes de finalizar el programa
        do{

        }while(true);
    }
}
