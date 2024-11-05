package keyboard;

public class KeyPressReader {

    static {
        System.loadLibrary("key_reader"); // The shared object file will be named `libkey_reader.so`
    }

    // Native method declaration
    /**
     * The method waits for any key pressed, then it returns the pressed char
     * @return The pressed char at the keyboard
     */
    static public native int getKey();

    /**
     * The method waits for any key pressed, if the selected timeout ends, the function gets out,
     * and returns -1. If there is any pressed key before the timeout, the function returns the ASCII code of the
     * pressed key.
     * If the pressed key does not have an explicit ASCII respective value, this class provides constant values
     * to compare to the function return, in order to detect other common keys.
     * @param timeoutMs : the timeout in milliseconds
     */
    static public native int getKeyTimeout(int timeoutMs);


    // Define constants for special keys
    public static final int ARROW_UP = 1001;
    public static final int ARROW_DOWN = 1002;
    public static final int ARROW_LEFT = 1003;
    public static final int ARROW_RIGHT = 1004;

    public static final int BACKSPACE = 127;
    public static final int INTRO = 10;
    public static final int TAB = 9;
}
