package main;

import java.util.ArrayList;
import song.Song;

public class ConsoleMenu {
    // ANSI escape sequences for text formatting and colors
    private static final String BOLD = "\u001B[1m";
    private static final String ITALIC = "\u001B[3m";
    private static final String UNDERLINE = "\u001B[4m";
    private static final String STRIKETHROUGH = "\u001B[9m";

    // Char colors
    private static final String GRAY_CHAR = "\u001B[30m";
    private static final String RED_CHAR = "\u001B[31m";
    private static final String GREEN_CHAR = "\u001B[32m";
    private static final String YELLOW_CHAR = "\u001B[33m";
    private static final String BLUE_CHAR = "\u001B[34m";
    private static final String PINK_CHAR = "\u001B[35m";
    private static final String CIAN_CHAR = "\u001B[36m";

    // Pastel char colors
    private static final String PASTEL_RED = "\u001B[91m";
    private static final String PASTEL_GREEN = "\u001B[92m";
    private static final String PASTEL_YELLOW = "\u001B[93m";
    private static final String PASTEL_BLUE = "\u001B[94m";
    private static final String PASTEL_PINK = "\u001B[95m";
    private static final String PASTEL_CIAN = "\u001B[96m";

    // Background colors
    private static final String RED_BG = "\u001B[41m";
    private static final String GREEN_BG = "\u001B[42m";
    private static final String YELLOW_BG = "\u001B[43m";
    private static final String BLUE_BG = "\u001B[44m";
    private static final String PINK_BG = "\u001B[45m";
    private static final String CIAN_BG = "\u001B[46m";
    private static final String WHITE_BG = "\u001B[47m";

    // Pastel background colors
    private static final String PASTEL_GRAY_BG = "\u001B[100m";
    private static final String PASTEL_RED_BG = "\u001B[101m";
    private static final String PASTEL_GREEN_BG = "\u001B[102m";
    private static final String PASTEL_YELLOW_BG = "\u001B[103m";
    private static final String PASTEL_BLUE_BG = "\u001B[104m";
    private static final String PASTEL_PINK_BG = "\u001B[105m";
    private static final String PASTEL_CIAN_BG = "\u001B[106m";
    private static final String PASTEL_WHITE_BG = "\u001B[107m";

    // Emojis
    public static final String LEFT_ARROW = "\u2190";
    public static final String RIGHT_ARROW = "\u2192";
    public static final String UP_ARROW = "\u2191";
    public static final String DOWN_ARROW = "\u2193";
    public static final String PLAY = "\u25B6";
    public static final String PAUSE = "\u275A\u275A";

    // Reset color
    private static final String RESET = "\u001B[0m";

    // Text alignement
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 1;

    // ##################### instance properties #####################
    private ArrayList<Song> menuItems;
    private int selectedItemIndex = -1;
    private int consoleWidth;

    // ##################### constructor #####################
    public ConsoleMenu(int consoleWidth) {
        this.consoleWidth = consoleWidth;
    }
    
    /**
     * Clears the screen and places the cursor to the top left
     */
    public void clearScreen() {
        System.out.print("\u001B[2J\u001B[H"); // Clear screen and reset cursor to top left
    }

    /**
     * Clears the given row
     */
    public void clearRow(int row) {
        System.out.print("\u001B[" + row + ";1H" + "\u001B[2K"); // Move to row, clear the entire line
    }

    /**
     * Sets the cursor at the beginning of a specific row
     */
    public void setCursor(int row) {
        System.out.print("\u001B[" + row + ";1H");
    }

    /**
     * Method to set the cursor to a specific row and column
     */
    public void setCursor(int row, int column) {
        System.out.print(String.format("\u001B[%d;%dH", row, column));
    }

    /**
     * Prints the given text with the given configuration
     * @param row
     * @param column
     * @param text
     * @param alignment
     */
    public void printText(int row, int column, String text, int alignment) {
        int padding = 0;
        if (alignment == ALIGN_CENTER) {
            padding = (consoleWidth - text.length()) / 2;
        } else if (alignment == ALIGN_RIGHT) {
            padding = consoleWidth - text.length();
        }
        System.out.printf("\u001B[%d;%dH%s%s\n", row, column + padding, text, RESET);
    }

    /**
     * Prints the given song with the given configuration
     * @param row
     * @param column
     * @param song
     * @param alignment
     */
    public void printText(int row, int column, Song song, int alignment) {
        int padding = 0;
        String songText = songPrintableString(song);

        if (alignment == ALIGN_CENTER) {
            padding = (consoleWidth - songText.length()) / 2;
        } else if (alignment == ALIGN_RIGHT) {
            padding = consoleWidth - songText.length();
        }
        System.out.printf("\u001B[%d;%dH%s%s\n", row, column + padding, songText, RESET);
    } 

    /**
     * Method to create and display a loading bar with current and total time in mm:ss format
     */
    public void printLoadingBar(int row, long actualSeconds, long totalSeconds) {
        clearRow(row); // Clear the row before printing

        // Calculate the percentage based on actual time and total time
        int percentage = (int) ((double) actualSeconds / totalSeconds * 100);

        // Convert seconds to mm:ss format for both actual time and total time
        String currentTime = formatTime(actualSeconds);
        String totalTime = formatTime(totalSeconds);

        // Calculate the filled and empty parts of the bar based on the percentage
        int fillWidth = (consoleWidth * percentage) / 100;
        String bar = "#".repeat(fillWidth) + "-".repeat(consoleWidth - fillWidth);

        // Print the bar with the current and total times on the left and right
        System.out.printf("\u001B[%d;%dH%s %s%s %s%s\n", row, 0, currentTime, bar, RESET, totalTime, RESET);
    }

    /**
     * Helper method to format seconds as mm:ss
     * @return Converts the given seconds to a string with the following format -> mm:ss
     */
    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    /**
     * Displays the loaded list
     * @param leftMargin
     * @param maxItems  The items to be displayed
     */
    public void displayMenu(int leftMargin, int maxItems) {
        System.out.print("\nasdfasdfasdf");
        for (int i = 0; (i < menuItems.size()) && (i < maxItems); i++) {
            setCursor(0); // Example: Start menu items from row 2
            if (i == selectedItemIndex) {
                System.out.printf("\u001B[%d;%dH%s%s- %s%s", i + 2, leftMargin, WHITE_BG, BOLD, songPrintableString(menuItems.get(i)), RESET);
            } else {
                System.out.printf("\u001B[%d;%dH- %s", i + 2, leftMargin, songPrintableString(menuItems.get(i)));
            }
        }
    }

    /**
     * Updates the selected menu item with white background and black text
     */
    public void setSelectedItem(int index, int leftMargin, int maxItems) throws IndexOutOfBoundsException {

        if (index >= menuItems.size()) {
            throw new IndexOutOfBoundsException();
        }

        clearScreen();
        if (index >= 0 && (index < menuItems.size())) {
            selectedItemIndex = index;
            displayMenu(leftMargin, maxItems); // Re-display the menu with the selected item highlighted
        }
    }

    // Setter for menu items
    public void setMenuItems(ArrayList<Song> items) {
        this.menuItems = items;
    }

    // ##################### private methods #####################
    /**
     * Creates a more visual string from a song to print at the list
     */
    static private String songPrintableString(Song song) {
        String buff = song.getName();
        for (int i = 0; i < song.getAuthorsCount(); i++) {
            buff += " - " + song.getAuthor(i);
        }

        return buff;
    }

    
}

