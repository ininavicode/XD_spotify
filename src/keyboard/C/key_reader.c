#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <jni.h>

// Define constants for special keys
#define KEY_ARROW_UP    1001
#define KEY_ARROW_DOWN  1002
#define KEY_ARROW_LEFT  1003
#define KEY_ARROW_RIGHT 1004

// Function to read a single key press or detect special keys
int get_key_press() {
    struct termios oldt, newt;
    int ch;

    // Get current terminal settings and modify them for raw mode
    tcgetattr(STDIN_FILENO, &oldt);
    newt = oldt;
    newt.c_lflag &= ~(ICANON | ECHO); // Turn off canonical mode and echo
    tcsetattr(STDIN_FILENO, TCSANOW, &newt);

    // Read the first character
    ch = getchar();

    // Check if the first character indicates a special key (ESC sequence)
    if (ch == 27) { // ESC character
        // Read the next two characters to check for arrow keys
        int next1 = getchar();
        int next2 = getchar();

        if (next1 == 91) { // '[' character after ESC
            switch (next2) {
                case 65: ch = KEY_ARROW_UP; break;    // Arrow Up
                case 66: ch = KEY_ARROW_DOWN; break;  // Arrow Down
                case 67: ch = KEY_ARROW_RIGHT; break; // Arrow Right
                case 68: ch = KEY_ARROW_LEFT; break;  // Arrow Left
                default: break;
            }
        }
    }

    // Restore original terminal settings
    tcsetattr(STDIN_FILENO, TCSANOW, &oldt);

    return ch;
}

// JNI function to expose to Java
JNIEXPORT jint JNICALL Java_KeyPressReader_getKey(JNIEnv *env, jobject obj) {
    return get_key_press();
}
