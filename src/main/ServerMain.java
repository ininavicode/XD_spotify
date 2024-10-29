package main;

import java.io.IOException;
import java.net.ProtocolException;
import java.rmi.server.ServerCloneException;
import javax.net.ssl.SSLSessionBindingListener;
import protocol.*;
import song.*;
import java.util.ArrayList;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        
        Protocol.Server server = new Protocol.Server(12000);
        
        while (true) { 
            System.out.print("\nWaiting for packets");
            server.waitForPacket();
            Protocol.COMMAND_TYPE commandType = server.getLastPacketCommandType();
            System.out.print("\nCommand type: " + commandType.name());
            System.out.print("\nSong name: " + SongList.fromByteRaw(server.getLastPacket_SongName().getBytes()));
            System.out.print("\nClient ID: " + server.getLastPacket_ClientID());

            ArrayList<Song> searchEngineByPassResponse = SongList.fromByteRaw("Tocado y hundido;Melendi\nTomame o dejame;Naiara".getBytes());

            System.out.print("\nSending response to client");
            server.Response_searchEngine((short)1, searchEngineByPassResponse);
        }
        
    }
}
