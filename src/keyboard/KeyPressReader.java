public class KeyPressReader {

    static {
        // Load the C library (shared object)
        System.loadLibrary("key_reader"); // `libkey_reader.so`
    }

    // Native method declaration
    /**
     * The method waits for any key pressed, then it returns the pressed char
     * @return The pressed char at the keyboard
     */
    public static native char getKey();

    // Define constants for special keys
    public static final int ARROW_UP = 1001;
    public static final int ARROW_DOWN = 1002;
    public static final int ARROW_LEFT = 1003;
    public static final int ARROW_RIGHT = 1004;

    public static final int BACKSPACE = 127;
    public static final int INTRO = 10;
    public static final int TAB = 9;
}
