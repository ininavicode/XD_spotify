public class Main {
    public static void main(String[] args) {
        int timeout = 5; // Initial timeout in seconds

        System.out.println("Press any key to reset the timeout. Timeout set to " + timeout + " seconds...");

        while (true) {
            int key = KeyPressReader.getKeyTimeout(1000);
            if (key != -1) { // A key was pressed
                System.out.println("You pressed: " + key);
                if (key == 'q') { // Exit on 'q' key press
                    break;
                }
                // Reset the timeout if any key is pressed
                timeout = 5;
            } else {
                // Timeout occurred
                System.out.println("Timeout occurred. Exiting...");
                break;
            }
        }
    }
}
