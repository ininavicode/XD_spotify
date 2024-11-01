package main;

import java.io.IOException;
import protocol.*;
import song.*;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        
        Client client = new Client("127.0.0.1", 12000);

        System.out.print("\nClick enter to send");
        System.in.read();
        
        // ##################### REQUEST SEARCH ENGINE #####################
        // client.RequestSearchEngine("Tomame o dejame Naiara", (short)-1);

        // System.out.print("\nWaiting for server response");
        // Protocol.ResponseSearchEngine_t response = client.ReceiveSearchEngine();

        // System.out.print("\nServer response: " + response.songList);
        
        // System.out.print("\n");

        // ##################### REQUEST MP3 #####################
        // client.RequestReceiveMP3(new Song("Song Name", "Author"), "data/str_rec.txt");  // request a text test file
        // FIXED: It seems like there is lost data on the first 1000 packets, just before the specific packet request start happenning,
        //  Any packet received after the use of the specific packet request is correctly pasrse, thougth, the received song is reproduced
        //  perfectly skipping the initial seconds. The str file is parsed with fails, for example at the line 623 of the str_rec.txt
        //  is notable that there is a lack or excess of "\n" at some point
        // -rw-r--r-- 1 javi javi 8952125 Oct 21 09:38 song.mp3
        // -rw-r--r-- 1 javi javi 8950291 Oct 31 08:30 song_rec.mp3
        // -rw-r--r-- 1 javi javi 4173201 Oct 31 08:46 str_rec.txt
        // -rw-r--r-- 1 javi javi 4175999 Oct 31 08:44 tosend.txt

        client.requestReceiveFile(new Song("Song Name", "Author"), "data/song_rec.mp3");  // request the mp3
        // FIXED: Check why nulls are pasted in strtest.txt (does it matter for the final version???).
    }
}
