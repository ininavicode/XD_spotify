public class Main {
    public static void main(String[] args) {
        int timeout = 1000; // Initial timeout in ms
        String acumStr = "";

        System.out.println("Press any key to reset the timeout. Timeout set to " + timeout + " seconds...");

        // the first character is getted without timeout
        int key = (int)KeyPressReader.getKey();

        while (true) {

            if (key == -1) {    // timeout
                // Timeout occurred
                System.out.println("Timeout occurred. Exiting...");
                break;

            } else if (key == KeyPressReader.BACKSPACE) {
                int length = acumStr.length();
                if (length > 0) {

                    acumStr = acumStr.substring(0, length - 1);
                }

            } else if (key == 'q') {
                break;

            } else {
                // other pressed keys
                acumStr += (char)(key);
                
            }

            // ANSII escape code to clear the actual line of the command line 
            // "\033[2K\r"
            System.out.print("\033[2K\r" + acumStr);

            key = KeyPressReader.getKeyTimeout(timeout);


        }
    }
}
