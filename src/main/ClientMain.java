package main;

import java.io.IOException;
import java.net.UnknownHostException;

import keyboard.KeyPressReader;
import menu.Menu;
import protocol.*;
import search_engine.*;
import song.*;
import mp3_player.*;

public class ClientMain {
    // Constantes
    private static final String DATAPATH = "data/";
    private static final int TIMEOUT = 5000;

    // Variables
    private static String input;
    private static int key;
    private static Menu menu;
    private static char pressedChar;
    private static Protocol.ResponseSearchEngine_t response;
    private static Client client;
    private static SearchEngine historialOfSearches;
    private static VLCJPlayer mp3Player;
    private static Song selected ;
    private static String dir;

    public static void main(String[] args) throws IOException, UnknownHostException {
        
        inicializaciones();

        while (true) {
            if (menu != null) {
                menu.render();
            }
            key = KeyPressReader.getKeyTimeout(TIMEOUT);
            System.out.println(key);
            if (key != -1) {
                if (key == 'q') {
                    break;
                }
                else if (menu != null && key == KeyPressReader.ARROW_UP) {
                    menu.selectSong(false);
                    System.out.println(menu.getSelectedIndex());
                }
                else if (menu != null && key == KeyPressReader.ARROW_DOWN) {
                    menu.selectSong(true);
                    System.out.println(menu.getSelectedIndex());
                }
                else if (key == KeyPressReader.BACKSPACE) {
                    input = input.substring(0,input.length() - 1);
                    clearConsole();
                    System.out.println(input);
                }
                else if (key == KeyPressReader.INTRO) {
                    selected = menu.getSelectedSong();
                    dir = historialOfSearches.getMP3ByName(selected);
                    if(dir == null) {
                        // Song not available, request to server made.
                        // TODO: Add the code to transform the song to the name that will be sent to the server.
                        dir = selected.toFilename();
                        client.requestReceiveFile(selected, DATAPATH + dir);
                        historialOfSearches.addSong(selected, dir); // Add the searched song to the directory.
                    }
                    // If available, automatically play it.
                    mp3Player.play(DATAPATH + dir); // TODO: Revise the format of the mp3 saving.                
                } else {
                    pressedChar = (char) key;
                    input+=pressedChar;
                    clearConsole();
                    System.out.println(input);
                }
            } else { // If timeout occurs, search for the list of songs.
                client.requestSearchEngine(input, -1L);
                response = client.receiveSearchEngine();
                menu = new Menu(response.songList);
            }
        }
    }

    private static void clearConsole() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void inicializaciones() {
        try {
            // ##################### main variables #####################
            client = new Client("127.0.0.1", 12000);  // Aquí ocurre el error
            historialOfSearches = new SearchEngine();
            mp3Player = new VLCJPlayer();
            menu = new Menu(historialOfSearches.getSongsList());
            input = "";
        } catch (UnknownHostException e) {
            System.err.println("Error: No se pudo resolver el nombre del host.");
            e.printStackTrace();
            // Manejar el error de alguna manera (por ejemplo, salir o intentar otro host)
        } catch (IOException e) {
            System.err.println("Error: Error al crear la conexión al servidor.");
            e.printStackTrace();
        }
    }
    
}
