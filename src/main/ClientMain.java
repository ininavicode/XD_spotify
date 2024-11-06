package main;

import java.io.IOException;

import keyboard.KeyPressReader;
import menu.Menu;
import protocol.*;

public class ClientMain {

    private static final int TIMEOUT = 5000;
    public static void main(String[] args) throws IOException {
        // ##################### main variables #####################
        Client client = new Client("127.0.0.1", 12000);

        String input = "";
        int key;
        Menu menu = null;
        char pressedChar;

        while (true) {
            if (menu != null) {
                menu.render();
            }
            key = KeyPressReader.getKeyTimeout(TIMEOUT);
            if (key != -1) {
                if (key == 'q') {
                    break;
                }
                else if (menu != null && key == KeyPressReader.ARROW_UP) {
                    menu.selectSong(false);
                }
                else if (menu != null && key == KeyPressReader.ARROW_DOWN) {
                    menu.selectSong(true);
                }
                else if (key == KeyPressReader.BACKSPACE) {
                    input = input.substring(0,input.length() - 1);
                    clearConsole();
                    System.out.println(input);
                }
                else if (key == KeyPressReader.INTRO) {
                    
                } else {
                    pressedChar = (char) key;
                    input+=pressedChar;
                    clearConsole();
                    System.out.println(input);
                }
            } else { // En caso de TIMEOUT enviamos peticion al servidor para recibir la lista de canciones
                client.requestSearchEngine(input, -1L);
                Protocol.ResponseSearchEngine_t response = client.receiveSearchEngine();
                menu = new Menu(response.songList);
                System.out.println("HOLA");
            }
        }
    }

    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
