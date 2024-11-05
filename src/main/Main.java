public class Main {
    public static void main(String[] args) {
        KeyPressReader reader = new KeyPressReader();
        System.out.println("Press any key to see its code (Press 'q' to exit)...");

        while (true) {
            int key = reader.getKey(); // Get key as int

            // Interpret the key press
            switch (key) {
                case KeyPressReader.ARROW_UP:
                    System.out.println("You pressed: Arrow Up");
                    break;
                case KeyPressReader.ARROW_DOWN:
                    System.out.println("You pressed: Arrow Down");
                    break;
                case KeyPressReader.ARROW_LEFT:
                    System.out.println("You pressed: Arrow Left");
                    break;
                case KeyPressReader.ARROW_RIGHT:
                    System.out.println("You pressed: Arrow Right");
                    break;
                case 'q':
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("You pressed: " + key);
                    break;
            }
        }
    }
}
