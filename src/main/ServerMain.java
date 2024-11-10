package main;

import java.io.IOException;
import protocol.*;
import song.*;

public class ServerMain {

    static final private String DATA_PATH = "dataserver/";
    public static void main(String[] args) throws IOException, IllegalArgumentException {
        // ##################### checking args #####################
        // user should invoke the program with the <IP> <port> arguments
        if (args.length != 1) {
            throw new IllegalArgumentException("\nInvalid arguments invoking main. Ivoke main with the following parameters\n\t<listeningPort>");
        }
        
        Server server = new Server(Integer.parseInt(args[0]), DATA_PATH + "available_songs.csv");
        
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
                    System.out.printf("\nCookie: %x%n", server.getLastPacket_Cookie());

                    Protocol.ResponseSearchEngine_t result = server.makeSearch();
                    System.out.printf("\nSending response to client with cookie: %x%n", (result.cookie));
                    server.responseSearchEngine(new Protocol.ResponseSearchEngine_t(result.cookie, result.songList));

                    break;
                case SONG_MP3_N_PACKETS_REQUEST:
                    System.out.print("\nSong name: " + server.getLastPacket_SongName());
                    System.out.printf("\nLast valid cookie: %x%n", server.getLastPacket_Cookie());

                    server.responseFilePacketsSize(DATA_PATH + Song.songStringToFilename(server.getLastPacket_SongName()));

                    break;
                case SONG_MP3_PACKETS_RANGE_REQUEST:
                    System.out.print("\nStart Packet ID: " + server.getLastPacket_StartPacketID());
                    System.out.print("\nEnd Packet ID: " + server.getLastPacket_EndPacketID());
                    // TODO: Finish to sending of the packets based on the cookie name sesison
                    server.responseFilePacketsRange(server.getLastPacket_Cookie(), server.getLastPacket_StartPacketID(), server.getLastPacket_EndPacketID());  // send the mp3

                    break;
                case FINISH_COMM:
                    System.out.printf("\nFinalising session with cookie: %x%n", server.getLastPacket_Cookie());
                    server.closeSession(server.getLastPacket_Cookie());
                    break;

                default:
                    break;
            }

            
        }
        
    }

    // ##################### DEPRECATED INITIALISATION #####################
    // /**
    //  * Method to create the session handler + search engine instance.
    //  * 
    //  * @param server The instance of the sever from which the handler will be generated, 
    //  *               to force the existance of one.
    //  * @return The instance of the session handler.
    //  */
    // private static SessionHandler initialListOfSongs() {
    //     // ArrayList<Song> songList;

    //     // try {
    //     //     songList = SongList.fromFile(DATA_PATH + "song_names.csv");
            
    //     // } catch (FileNotFoundException e) {
    //     //     System.err.printf("\nFile %s not found", DATA_PATH + "song_names.csv");
    //     //     songList = new ArrayList<Song>(0);
    //     // }


    //     // Map.Entry<Song, String>[] mapEntries = new Map.Entry[songList.size()];

    //     // int i = 0;
    //     // for (Song song : songList) {
    //     //     mapEntries[i++] = Map.entry(song, song.toFilename());
    //     // }

    //     return new SessionHandler(DATA_PATH + "available_songs.csv");

    //     // return server.new SessionHandler(Map.ofEntries(Map.entry(new Song("Family Business", "Kanye West"), "data/Family-Business.mp3"),
    //     //                                                Map.entry(new Song("ISABELLA", "Kanye West, Lil Nas X"), "data/ISABELLA.mp3")
    //     //                                               ));
    // }


}
