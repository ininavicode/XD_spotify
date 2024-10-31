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
        // client.RequestSearchEngine(new RequestSearchEngine_t((short)-1, "Tomame o dejame Naiara"));

        // System.out.print("\nWaiting for server response");
        // Protocol.ResponseSearchEngine_t response = client.ReceiveSearchEngine();

        // System.out.print("\nServer response: " + response.songList);
        
        // System.out.print("\n");

        // ##################### REQUEST MP3 #####################
        client.RequestReceiveMP3(new Song("Song Name", "Author"), "data/str_rec.txt");  // request a text test file
        // FIXME: The request of specific packets start happenning arround 2000 packets to send. If this type of request is not used
        //  the file arrives perfectly, but when this is needed, the parsed file is incorrect
        // The original file has 2000 more bytes than the received, so there migth be data loss at the use of this request
        // For files with less than 1000, the packet specific request does not happen, and the parsed file is exactly the expected
        // -rw-r--r-- 1 javi javi 8952125 Oct 21 09:38 song.mp3
        // -rw-r--r-- 1 javi javi 8950291 Oct 31 08:30 song_rec.mp3
        // -rw-r--r-- 1 javi javi 4173201 Oct 31 08:46 str_rec.txt
        // -rw-r--r-- 1 javi javi 4175999 Oct 31 08:44 tosend.txt

        // client.RequestReceiveMP3(new Song("Song Name", "Author"), "data/song_rec.mp3");  // request the mp3
        // FIXED: Check why nulls are pasted in strtest.txt (does it matter for the final version???).
    }
}
