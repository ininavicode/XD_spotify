package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import protocol.*;
import protocol.Server.SessionHandler;
import song.*;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        
        Server server = new Server(12000);
        Server.SessionHandler sessionHandler = initialListOfSongs(server);
        
        // Infinite loop.
        while (true) { 
            System.out.print("\nWaiting for packets");
            server.waitForPacket();

            Protocol.COMMAND_TYPE commandType = server.getLastPacketCommandType();

            System.out.print("\nCommand type: " + commandType.name());

            // TODO: Add the nested class PACKET into the server, so each time the server
            //  gets a packet, all the info is saved there.
            // There should be as many sub-types of packets as request types, so the server saves an
            //  instance of the proper packet sub-class to the last packet property.
            // The last packet property should be accessible with a method but not modified, so it is
            //  necessary to implement getters for the packet sub-class.
            switch (commandType) {
                case SEARCH_ENGINE_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());
                    System.out.print("\nCookie: " + server.getLastPacket_Cookie());

                    Protocol.ResponseSearchEngine_t result = sessionHandler.makeSearch(server.getLastPacket_Cookie(), server.getLastPacket_SongName());
                    System.out.println("\nSending response to client with cookie: " + result.cookie);
                    server.responseSearchEngine(new Protocol.ResponseSearchEngine_t(result.cookie, result.songList));

                    break;
                case SONG_MP3_N_PACKETS_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());

                    server.responseFilePacketsSize("data/song.mp3");

                    break;
                case SONG_MP3_PACKETS_RANGE_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());
                    System.out.print("\nStart Packet ID: " + server.getLastPacket_StartPacketID());
                    System.out.print("\nEnd Packet ID: " + server.getLastPacket_EndPacketID());

                    server.responseFilePacketsRange("data/song.mp3", server.getLastPacket_StartPacketID(), server.getLastPacket_EndPacketID());  // send the mp3

                    break;

                default:
                    break;
            }

            
        }
        
    }

    /**
     * Method to create the session handler + search engine instance.
     * 
     * @param server The instance of the sever from which the handler will be generated, 
     *               to force the existance of one.
     * @return The instance of the session handler.
     */
    private static Server.SessionHandler initialListOfSongs(Server server) {
        return server.new SessionHandler(Map.ofEntries(Map.entry(new Song("Family Business", "Kanye West"), "data/Family-Business.mp3"),
                                                       Map.entry(new Song("ISABELLA", "Kanye West, Lil Nas X"), "data/ISABELLA.mp3")
                                                      ));
    }

}
