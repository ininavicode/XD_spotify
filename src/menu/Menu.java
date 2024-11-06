package menu;

import java.util.List;
import java.util.ArrayList;
import song.*;

public class Menu {
    
    private static final String SELECTED_MARK = " -> ";  // Marca que se pondrá al lado de la canción seleccionada
    private List<Song> songs;  // Lista de canciones
    private int selectedIndex;   // Índice de la canción seleccionada

    public Menu(ArrayList<Song> lista) {
        this.songs = lista;
        this.selectedIndex = 0;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public Song getSelectedSong() {
        return songs.get(selectedIndex);
    }

    public void incrementarIndice() {
        if (selectedIndex >= 0 && selectedIndex < songs.size() - 1) {
            selectedIndex += 1;
        }
    }

    public void decrementarIndice() {
        if (selectedIndex > 0) {
            selectedIndex -= 1;
        }
    }

    // Método para renderizar el menú
    public void render() {
        // Limpiar consola antes de mostrar el nuevo menú
        // clearConsole();

        System.out.println("Lista de Canciones:");
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            if (i == selectedIndex) {
                System.out.println(song + SELECTED_MARK);  // Marca la canción seleccionada
            } else {
                System.out.println(song);  // Muestra la canción sin la flecha
            }
        }

        System.out.println("\nSelecciona una canción con las teclas de dirección o 'q' para salir.");
    }
}
