package main;

import java.io.IOException;
import java.util.ArrayList;
import protocol.Protocol;
import song.*;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        
        Protocol.Client client = new Protocol.Client("127.0.0.1", 12000);

        System.out.print("\nClick something to send");
        System.in.read();
        client.Request_searchEngine("Tomame o dejame;Naiara");

        System.out.print("\nWaiting for server response");
        ArrayList<Song> songList = client.Receive_searchEngine();

        System.out.print("\nServer response: " + songList);
        System.out.print("\nAssigned clientID: " + client.getClientID());
        
        System.out.print("\n");
    }
}
