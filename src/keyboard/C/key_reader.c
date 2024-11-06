#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <sys/select.h>
#include <jni.h>
#include "keyboard_KeyPressReader.h"

// Define constants for special keys
#define KEY_ARROW_UP    1001
#define KEY_ARROW_DOWN  1002
#define KEY_ARROW_LEFT  1003
#define KEY_ARROW_RIGHT 1004

// Function to read a single key press with a timeout
int get_key_press_with_timeout(int timeout_ms) {
    struct termios oldt, newt;
    fd_set set;
    struct timeval timeout;
    int rv;
    int ch = -1; // -1 will indicate no key press within timeout

    // Get current terminal settings and modify them for raw mode
    tcgetattr(STDIN_FILENO, &oldt);
    newt = oldt;
    newt.c_lflag &= ~(ICANON | ECHO); // Turn off canonical mode and echo
    tcsetattr(STDIN_FILENO, TCSANOW, &newt);

    // Set up the file descriptor set for select
    FD_ZERO(&set);
    FD_SET(STDIN_FILENO, &set);

    // Set up the timeout struct
    timeout.tv_sec = timeout_ms / 1000;
    timeout.tv_usec = (timeout_ms % 1000) * 1000;

    // Wait for input with a timeout
    rv = select(STDIN_FILENO + 1, &set, NULL, NULL, &timeout);
    if (rv > 0) {
        // Read a single character if there was input
        ch = getchar();

        // Check if the character indicates a special key (ESC sequence)
        if (ch == 27) { // ESC character
            // Peek ahead for additional characters to identify special keys
            int next1 = getchar();
            if (next1 == 91) { // '[' character after ESC
                int next2 = getchar();
                switch (next2) {
                    case 65: ch = KEY_ARROW_UP; break;    // Arrow Up
                    case 66: ch = KEY_ARROW_DOWN; break;  // Arrow Down
                    case 67: ch = KEY_ARROW_RIGHT; break; // Arrow Right
                    case 68: ch = KEY_ARROW_LEFT; break;  // Arrow Left
                    default: break;
                }
            }
        }
    }

    // Restore original terminal settings
    tcsetattr(STDIN_FILENO, TCSANOW, &oldt);

    return ch;
}
// JNI function to expose to Java
JNIEXPORT jint JNICALL Java_keyboard_KeyPressReader_getKeyTimeout(JNIEnv *env, jobject obj, jint timeout_ms) {
    return get_key_press_with_timeout(timeout_ms);
}


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
JNIEXPORT jint JNICALL Java_keyboard_KeyPressReader_getKey(JNIEnv *env, jobject obj) {
    return get_key_press();
}

