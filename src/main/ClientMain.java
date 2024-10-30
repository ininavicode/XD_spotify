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
        client.RequestReceiveMP3(new Song("Song Name", "Author"), "data/strtest.txt");
        // FIXED: Check why nulls are pasted in strtest.txt (does it matter for the final version???).
    }
}
