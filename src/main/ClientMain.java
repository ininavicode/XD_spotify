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
    private static final String AVAILABLE_SONGS_FILE = "data/available_songs.csv";
    private static final int TIMEOUT = 5000;
    private static final int WRITING_INF = 0;
    private static final int WRITING_TIME = 1;
    private static final int PLAYING = 2;
    private static final int SERVER_SEND = -1;
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
    //private static boolean shouldWait;

    public static void main(String[] args) throws IOException, UnknownHostException {
        
        inicializaciones();
        int state = WRITING_INF;
        
        while (true) {
            switch (state) {
                case WRITING_INF:
                    menu.render();
                    state = esperarDatos();
                    break;
                case WRITING_TIME:
                    menu.render();
                    state = esperarDatosUnTiempo();
                    break;
                case PLAYING:
                    clearConsole();
                    renderMenuPlayer();
                    state = reproducirCancion();
                    break;
                case SERVER_SEND:
                    state = enviarInfo();
                    break;
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
            historialOfSearches = new SearchEngine(AVAILABLE_SONGS_FILE);
            mp3Player = new VLCJPlayer();
            menu = new Menu(historialOfSearches.getSongsList());
            input = "";
            //shouldWait = false;
        } catch (UnknownHostException e) {
            System.err.println("Error: No se pudo resolver el nombre del host.");
            e.printStackTrace();
            // Manejar el error de alguna manera (por ejemplo, salir o intentar otro host)
        } catch (IOException e) {
            System.err.println("Error: Error al crear la conexión al servidor.");
            e.printStackTrace();
        }
    }

    private static int reproducirCancion() {
        selected = menu.getSelectedSong();
        dir = historialOfSearches.getMP3ByName(selected);
        if(dir == null) {
            // Song not available, request to server made.
            dir = selected.toFilename();
            try {
                client.requestReceiveFile(selected, DATAPATH + dir);
            }
            catch (IOException e) {
                System.err.println("Error: Error al crear la conexión al servidor.");
                e.printStackTrace();
            }
            historialOfSearches.addSong(selected, dir); // Add the searched song to the directory.
        }
        // If available, automatically play it.
        mp3Player.play(DATAPATH + dir);

        boolean salir = false;
        boolean pausa = false;
        while (!salir) {
            key = KeyPressReader.getKey();
            switch (key) {
                case KeyPressReader.Q_MAYUS:
                    clearConsole();
                    salir = true;
                    break;
                case KeyPressReader.Q_MINUS:
                    clearConsole();
                    salir = true;
                    break;
                case KeyPressReader.SPACE:
                    if (pausa) {
                        mp3Player.play();
                        pausa = false;

                    } else {
                        mp3Player.pause();
                        pausa = true;
                    }
                    break;
                case KeyPressReader.ARROW_RIGHT:
                    mp3Player.advance10Seconds();
                    break;
                case KeyPressReader.ARROW_LEFT:
                    mp3Player.goBack10Seconds();
                    break;
            }
        }
        return WRITING_INF;
    }
    
    private static int esperarDatos() {
        int res;

        key = KeyPressReader.getKey();
        if (key == KeyPressReader.ARROW_UP) {
            menu.decrementarIndice();
            clearConsole();
            res = WRITING_TIME;
        }
        else if (key == KeyPressReader.ARROW_DOWN) {
            menu.incrementarIndice();
            clearConsole();
            res = WRITING_TIME;
        }
        else if (key == KeyPressReader.BACKSPACE) {
            if (input.length() > 0) {
                input = input.substring(0, input.length() - 1);
            }
            //clearConsole();
            res = WRITING_TIME;
        }
        else if (key == KeyPressReader.INTRO) {
            res = PLAYING;     
        } else {
            pressedChar = (char) key;
            input+=pressedChar;
            clearConsole();
            System.out.println(input);
            res = WRITING_TIME;
        }
        // Pedimos la informacion al servidor
        try {
            client.requestSearchEngine(input, -1L);
            response = client.receiveSearchEngine();
            menu = new Menu(response.songList);
        }
        catch (IOException e) {
            System.err.println("Error: Error al crear la conexión al servidor.");
            e.printStackTrace();
        }
        
        return res;
    }

    private static int esperarDatosUnTiempo() {
        int res;
        key = KeyPressReader.getKeyTimeout(TIMEOUT);
        if (key != -1) {
            if (key == KeyPressReader.ARROW_UP) {
                menu.decrementarIndice();
                clearConsole();
                res = WRITING_TIME;
            }
            else if (key == KeyPressReader.ARROW_DOWN) {
                menu.incrementarIndice();
                clearConsole();
                res = WRITING_TIME;
            }
            else if (key == KeyPressReader.BACKSPACE) {
                if (input.length() > 0) {
                    input = input.substring(0, input.length() - 1);
                }
                //clearConsole();
                res = WRITING_TIME;
            }
            else if (key == KeyPressReader.INTRO) {
                res = PLAYING;             
            } else {
                pressedChar = (char) key;
                input+=pressedChar;
                clearConsole();
                System.out.println(input);
                res = WRITING_TIME;
            }
        } else {
            res = SERVER_SEND;
        }
        
        return res;
    }

    private static int enviarInfo() {
        try {
            client.requestSearchEngine(input, -1L);
            response = client.receiveSearchEngine();
            menu = new Menu(response.songList);
        }
        catch (IOException e) {
            System.err.println("Error: Error al crear la conexión al servidor.");
            e.printStackTrace();
        }

        return WRITING_INF;
    }

    public static void renderMenuPlayer() {
        System.out.println("----------- OPCIONES -----------");
        System.out.println("---- PAUSE/RESUME (SPACE) ------");
        System.out.println("-------- +10 SEC (--->) --------");
        System.out.println("-------- -10 SEC (<---) --------");
        System.out.println("---------- SALIR (Q) -----------");
    }
    
}
