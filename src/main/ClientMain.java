package main;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import keyboard.KeyPressReader;
import mp3.VLCJPlayer;
import protocol.Client;
import protocol.Protocol;
import search_engine.*;
import song.*;

public class ClientMain {
    // ##################### data config #####################
    static final private String DATA_PATH = "datacli/";

    // ##################### visualization condig #####################
    static final private int TEXT_INPUT_ROW = 10;
    static final private int SONG_NAME_ROW = 3;
    static final private int TIME_BAR_ROW = 10;

    // ##################### config constants #####################
    static final private int CONSOLE_WIDTH = 50;
    static final private int GET_KEY_TIMEOUT = 500; // ms
    
    static final private int MENU_ERROR_STATE = -1;
    static final private int MENU_TEXT_INPUT = 0;
    static final private int MENU_REPRODUCING = 1;
    static final private int MENU_REQUEST_SELECTED_SONG = 2;
    static final private int MENU_SEARCH_ENGINE_REQUEST = 3;
    
    // ##################### main variables #####################
    static private ConsoleMenu menu;

    static private String textInputString = "";

    static private int selectedSongIndex = -1;
    static private int menuState = 0;

    static private ArrayList<Song> songList;
    static private Client protocolClient;
    
    static private VLCJPlayer vlcPlayer;
    static private Object UdpdateTimeThreadSync = new Object();
    static private volatile boolean reproducingState = false;

    static private long clientCookie = -1; // any cookie added to this client by default

    /*
     * To indicate whether a timeout should be executed when waiting for a key input.
     */
    private static boolean establishTimeout = false;

    /*
     * Variable that will hold the local historial of songs available.
     */
    private static SearchEngine localHistorial;

    /*
     * Variable that indicates whether the song will be piled or played. 
     */
    private static boolean pileSong;
    

    // Demo method to test the class
    public static void main(String[] args) throws SocketException, UnknownHostException {
        // ##################### main initialization #####################
        menu = new ConsoleMenu(CONSOLE_WIDTH);
        menu.clearScreen();
        localHistorial = new SearchEngine(DATA_PATH + "available_songs.csv");
        songList = localHistorial.getSongsList(); // Initially, songlist will be the local historial of songs.

        protocolClient = new Client("127.0.0.1", 12000);

        vlcPlayer = new VLCJPlayer();
        
        // ##################### main loop #####################
        int key;
        Thread updateTimeThread = new Thread(ClientMain::MENU_REPRODUCING_updateTimeThread);
        updateTimeThread.start();

        // in case of error the application stays at the MENU_ERROR_STATE, an the captured message error will be printed
        String errorMsg = "";
        
        // Display historial songs, in case they exist.
        if(!songList.isEmpty()) {
            selectedSongIndex = 0;
            menu.setMenuItems(songList);
            menu.displayMenu(0);
            menu.setSelectedItem(0, 0);
        }

        while (true) {
            switch (menuState) {
                case MENU_TEXT_INPUT:

                    displayTextInput();
                    if(establishTimeout)
                        key = KeyPressReader.getKeyTimeout(GET_KEY_TIMEOUT);
                    else {
                        key = KeyPressReader.getKey();
                    }

                    // timeout if key == -1
                    if (key == -1) {
                        // ############ state change ############
                        menuState = MENU_SEARCH_ENGINE_REQUEST;
                        establishTimeout = false;
                        menu.clearScreen();
                        break;
                    }
                    else if (key == KeyPressReader.INTRO || key == KeyPressReader.TAB) {
                        if (!songList.isEmpty() && (selectedSongIndex != -1)) {
                            // ############ state change ############
                            menuState = MENU_REQUEST_SELECTED_SONG;
                            establishTimeout = false;
                            menu.clearScreen();
    
                            break;

                        }
                    }
                    else {
                        commonGetKeyTreatment(key);
                    }


                    break;

                case MENU_REPRODUCING:

                    key = KeyPressReader.getKey();

                    if (key == ' ') {
                        // mp3 stop/play
                        if (reproducingState == false) {
                            synchronized (UdpdateTimeThreadSync) {
                                reproducingState = true;
                                UdpdateTimeThreadSync.notify();
                                vlcPlayer.play(); // play the loaded file
                            }
                        }
                        else {
                            synchronized (UdpdateTimeThreadSync) {
                                reproducingState = false;
                                vlcPlayer.pause();
                                
                            }
                        }

                        // update the visualization state
                        displayReproducingState(reproducingState);

                    }
                    else if (key == KeyPressReader.ARROW_LEFT) {
                        // mp3 rewind
                        vlcPlayer.goBack10Seconds();
                        
                    }
                    else if (key == KeyPressReader.ARROW_RIGHT) {
                        // mp3 advance
                        vlcPlayer.advance10Seconds();

                    }
                    else if (key == 'q') {
                        // ############ state change ############
                        menuState = MENU_TEXT_INPUT;
                        vlcPlayer.pause();
                        menu.clearScreen();
                        menu.setSelectedItem(selectedSongIndex, 0);
                        if (!songList.isEmpty()) {
                            menu.setSelectedItem(selectedSongIndex, 0);
                        }
                        establishTimeout = true;
                        synchronized (UdpdateTimeThreadSync) {
                            reproducingState = false;
                        }
                        break;
                    }

                    break;

                case MENU_REQUEST_SELECTED_SONG:
                    establishTimeout = false; // No timeout till first letter is inputted in next MENU_TEXT_INPUT.
                    // ############ server and mp3 operations ############
                    String mp3ToPlay = songList.get(selectedSongIndex).toFilename();
                    Song newSong = Song.filenameToSong(mp3ToPlay);
                    if(!localHistorial.containsMP3(mp3ToPlay)) {
                        // Request to server if it does not exist in the local files.
                        try {
                            protocolClient.requestReceiveFile(songList.get(selectedSongIndex), DATA_PATH + mp3ToPlay);
                            localHistorial.addSong(newSong, mp3ToPlay); // add the song to the local historial in the human format.
                        } catch (IOException e) {
                            // ############ state change ############
                            menuState = MENU_ERROR_STATE;
                            errorMsg = e.getMessage();
                            break;
                        }
                    }

                    vlcPlayer.play(DATA_PATH + mp3ToPlay);

                    // ############ state change ############
                    menuState = MENU_REPRODUCING;
                    synchronized (UdpdateTimeThreadSync) {
                        reproducingState = true;
                        UdpdateTimeThreadSync.notify();
                    }

                    menu.clearScreen();
                    
                    menu.printText(SONG_NAME_ROW, 0, songList.get(selectedSongIndex), ConsoleMenu.ALIGN_LEFT);
                    displayReproducingState(reproducingState);

                    break;

                case MENU_SEARCH_ENGINE_REQUEST:
                    // protocol process ...
                    Protocol.ResponseSearchEngine_t response;
                    try {
                        protocolClient.requestSearchEngine(textInputString, clientCookie);
                        response = protocolClient.receiveSearchEngine();
                        
                    } catch (IOException e) {
                        // ############ state change ############
                        menuState = MENU_ERROR_STATE;
                        errorMsg = e.getMessage();
                        break;
                    }

                    // update songList

                    // TODO: the ArrayList<Song> has no clone method so all the usage of this structure is a reference copy
                    // This reference copy happens at the menu class to so there should be created a method to copy this structure
                    /*
                     * If the cookie returned is -1, it means that the client has erased all the content from
                     * the search bar, so the local historial of songs will be loaded on screen.
                     * In case an empty list is returned, which may happen if the user types some invalid
                     * data in, simply the empty song list will be displayed, indicating that the user
                     * has indeed no options with those search parameters.
                     */
                    // TODO: (optional) Indicate that an empty list has been returned instead of displaying void.
                    if(response.cookie == -1)
                        songList = localHistorial.getSongsList();
                    else
                        songList = response.songList; 

                    // update the cookie
                    clientCookie = response.cookie;

                    if (!songList.isEmpty()) {
                        if (selectedSongIndex >= songList.size()) {
                            selectedSongIndex = songList.size() - 1;
                        }
                        if (selectedSongIndex == -1) {
                            selectedSongIndex = 0;
                        }
                        menu.setMenuItems(songList);
                        menu.displayMenu(0);
                        menu.setSelectedItem(0, 0);
                    }
                    else {
                        selectedSongIndex = -1;
                    }

                    // ############ state change ############
                    menuState = MENU_TEXT_INPUT;
                    if (!songList.isEmpty()) {
                        menu.setSelectedItem(selectedSongIndex, 0);
                    }
                    
                    break;
            
                case MENU_ERROR_STATE:
                    System.err.print("\nError: " + errorMsg);
                    while (true) { 
                        
                    }
                }
            
        }

    } 

    private static void MENU_REPRODUCING_updateTimeThread() {
        while (true) {
            synchronized (UdpdateTimeThreadSync) {
                while (!reproducingState) {
                    try {
                        UdpdateTimeThreadSync.wait(); // Wait if the thread is paused
                    } catch (InterruptedException e) {
                        System.out.println("Update time thread interrupted.");
                        return; // Exit the loop if the thread is interrupted
                    }
                }
    
                // If not paused, update the time
                menu.printLoadingBar(TIME_BAR_ROW, vlcPlayer.getActualTime(), vlcPlayer.getTotalLength());
            }
    
            try {
                Thread.sleep(900); // Adjust the time as needed
            } catch (InterruptedException e) {
                System.out.println("Update time thread interrupted.");
                return; // Exit the loop if the thread is interrupted
            }
        }
    }

    static private void commonGetKeyTreatment(int key) {
        if ((key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z') || (key == ' ') || (key == KeyPressReader.BACKSPACE)) {
            establishTimeout = true;
            if (key == KeyPressReader.BACKSPACE) {
                if (textInputString.length() > 0) {
                    
                    textInputString = textInputString.substring(0, textInputString.length() - 1);
                }
            }
            else {
                textInputString += (char)key;
            }
            
        }
        else {
            // update select index
            establishTimeout = false;
            if (key == KeyPressReader.ARROW_UP) {
                if (selectedSongIndex > 0) {
                    selectedSongIndex--;
                }
            }
            else if  (key == KeyPressReader.ARROW_DOWN) {
                if (selectedSongIndex < (songList.size() - 1)) {
                    selectedSongIndex++;
                }
            }
            if (!songList.isEmpty()) {
                menu.setSelectedItem(selectedSongIndex, 0);
            }
        }
            
    }

    static private void displayTextInput() {
        menu.clearRow(TEXT_INPUT_ROW);
        menu.printText(TEXT_INPUT_ROW, 0, textInputString, ConsoleMenu.ALIGN_LEFT);
        menu.setCursor(TEXT_INPUT_ROW, textInputString.length());
    }

    static private void displayReproducingState(boolean state) {
        menu.clearRow(TIME_BAR_ROW - 1);
        if (state == true) {
            menu.printText(TIME_BAR_ROW - 1, 7, ConsoleMenu.LEFT_ARROW + "     " + ConsoleMenu.PLAY + "     " + ConsoleMenu.RIGHT_ARROW, ConsoleMenu.ALIGN_CENTER);

        }
        else {
            menu.printText(TIME_BAR_ROW - 1, 7, ConsoleMenu.LEFT_ARROW + "     " + ConsoleMenu.PAUSE + "     " + ConsoleMenu.RIGHT_ARROW, ConsoleMenu.ALIGN_CENTER);

        }

    }
}
