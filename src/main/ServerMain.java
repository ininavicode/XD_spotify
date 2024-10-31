package main;

import java.io.IOException;
import java.util.ArrayList;
import protocol.*;
import song.*;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        
        Server server = new Server(12000);
        
        while (true) { 
            System.out.print("\nWaiting for packets");
            server.waitForPacket();

            Protocol.COMMAND_TYPE commandType = server.getLastPacketCommandType();

            System.out.print("\nCommand type: " + commandType.name());

            switch (commandType) {
                case SEARCH_ENGINE_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());
                    System.out.print("\nClient ID: " + server.getLastPacket_ClientID());

                    ArrayList<Song> searchEngineByPassResponse = SongList.fromByteRaw(
                    "Tocado y hundido;Melendi\nTomame o dejame;Naiara\n".getBytes()
                    );

                    System.out.print("\nSending response to client");
                    server.ResponseSearchEngine(new Protocol.ResponseSearchEngine_t((short)1, searchEngineByPassResponse));

                    break;
                case SONG_MP3_N_PACKETS_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());

                    server.responseNPacketsOfSong("data/song.mp3");

                    break;
                case SONG_MP3_PACKETS_RANGE_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());
                    System.out.print("\nStart Packet ID: " + server.getLastPacket_StartPacketID());
                    System.out.print("\nEnd Packet ID: " + server.getLastPacket_EndPacketID());

                    server.responseMP3PacketRange("data/song.mp3", server.getLastPacket_StartPacketID(), server.getLastPacket_EndPacketID());  // send the mp3

                    break;

                default:
                    break;
            }

            
        }
        
    }
}
