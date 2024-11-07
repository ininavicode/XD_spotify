package main;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import keyboard.KeyPressReader;
import mp3.VLCJPlayer;
import protocol.Client;
import protocol.Protocol;
import song.Song;

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

    static private ArrayList<Song> songList = new ArrayList<>(0);

    static private boolean reproducingState;

    static private Client protocolClient;

    static private VLCJPlayer vlcPlayer;

    static private long clientCookie = -1; // any cookie added to this client by default

    // Demo method to test the class
    public static void main(String[] args) throws SocketException, UnknownHostException {
        // ##################### main initialization #####################
        menu = new ConsoleMenu(CONSOLE_WIDTH);
        menu.clearScreen();

        protocolClient = new Client("127.0.0.1", 12000);

        vlcPlayer = new VLCJPlayer();
        
        // ##################### main loop #####################
        int key;
        Thread updateTimeThread = new Thread(ClientMain::MENU_REPRODUCING_updateTimeThread);

        // in case of error the application stays at the MENU_ERROR_STATE, an the captured message error will be printed
        String errorMsg = "";

        while (true) {
            switch (menuState) {
                case MENU_TEXT_INPUT:
                    
                    key = KeyPressReader.getKey();

                    if (key == KeyPressReader.INTRO) {
                        if (!songList.isEmpty()) {
                            // ############ state change ############
                            menuState = MENU_REQUEST_SELECTED_SONG;
                            menu.clearScreen();
    
                            break;

                        }
                    }
                    else {
                        commonGetKeyTreatment(key);
                    }

                    key = KeyPressReader.getKeyTimeout(GET_KEY_TIMEOUT);

                    // timeout if key == -1
                    if (key == -1) {
                        if (textInputString.length() > 0) {
                            // ############ state change ############
                            menuState = MENU_SEARCH_ENGINE_REQUEST;
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
                        reproducingState = !reproducingState;
                        if (reproducingState == true) {
                            vlcPlayer.play(); // play the loaded file
                        }
                        else {
                            vlcPlayer.pause();
                        }

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
                        updateTimeThread.interrupt();
                        break;
                    }

                    break;

                case MENU_REQUEST_SELECTED_SONG:

                    // ############ server and mp3 operations ############
                    String mp3ToPlay = DATA_PATH + songList.get(selectedSongIndex).toFilename();
                    try {
                        protocolClient.requestReceiveFile(songList.get(selectedSongIndex), mp3ToPlay);
                        
                    } catch (IOException e) {
                        // ############ state change ############
                        menuState = MENU_ERROR_STATE;
                        errorMsg = e.getMessage();
                        break;
                    }

                    vlcPlayer.play(mp3ToPlay);

                    // ############ state change ############
                    menuState = MENU_REPRODUCING;
                    reproducingState = true;
                    menu.clearScreen();
                    
                    menu.printText(SONG_NAME_ROW, 0, songList.get(selectedSongIndex), ConsoleMenu.ALIGN_LEFT);
                    menu.printText(TIME_BAR_ROW - 1, 7, ConsoleMenu.LEFT_ARROW + "     " + ConsoleMenu.PAUSE + " / " + ConsoleMenu.PLAY + " (space)" + "     " + ConsoleMenu.RIGHT_ARROW, ConsoleMenu.ALIGN_CENTER);
                    // start the thread to update the actual time of the song at the MENU_REPRODUCING
                    updateTimeThread.start();

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
                    songList = response.songList; 

                    // update the cookie
                    clientCookie = response.cookie;

                    if (!songList.isEmpty()) {
                        selectedSongIndex = 0;
                        menu.setMenuItems(songList);
                        menu.displayMenu(0);
                        menu.setSelectedItem(0, 0);
                    }
                    else {
                        selectedSongIndex = -1;
                    }

                    // ############ state change ############
                    menuState = MENU_TEXT_INPUT;
                    displayTextInput();
                    
                    break;
            
                case MENU_ERROR_STATE:
                    System.err.print("\nError: " + errorMsg);
                    while (true) { 
                        
                    }
                }
            
        }

    } 

    private static void MENU_REPRODUCING_updateTimeThread() {
        long lastTime = vlcPlayer.getActualTime();

        while (true) {
            if (lastTime != vlcPlayer.getActualTime()) {
                menu.printLoadingBar(TIME_BAR_ROW, vlcPlayer.getActualTime(), vlcPlayer.getTotalLength());
                lastTime = vlcPlayer.getActualTime();
            }
            try {
                // Add a short sleep to avoid busy-waiting and allow for thread interruptions
                Thread.sleep(800); // Adjust the time as needed
            } catch (InterruptedException e) {

                return; // Exit the loop if the thread is interrupted
            }
        }
    }
    
    static private void commonGetKeyTreatment(int key) {
        if ((key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'A') || (key == ' ') || (key == KeyPressReader.BACKSPACE)) {
            if (key == KeyPressReader.BACKSPACE) {
                if (textInputString.length() > 0) {
                    
                    textInputString = textInputString.substring(0, textInputString.length() - 1);
                }
            }
            else {
                textInputString += (char)key;
            }

            displayTextInput();

        }
        else {
            // update select index
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
}
