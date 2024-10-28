package main;

import java.util.Arrays;
import packetizer.*;

public class test {

    static enum TestCase_Packetizer_nPacket {

        _str1_("data/str_file_test.txt", 0, 3, 3, "Exa".getBytes()),
        _str2_("data/str_file_test.txt", 1, 3, 3,"mpl".getBytes()),
        _strEnd_("data/str_file_test.txt", 4, 3, 2, "se".getBytes()),
        _strFirstEnd_("data/str_file_test.txt", 0, 15, 14, "Example phrase".getBytes());

        TestCase_Packetizer_nPacket(String input, int nPacket, int packetsSize, int expectedSize, byte[] expected) {
            this.input = input;
            this.nPacket = nPacket;
            this.packetsSize = packetsSize;
            this.expectedSize = expectedSize;
            this.expected = expected;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;

        // ##################### properties #####################
        private String input;
        private int nPacket;
        private int packetsSize;
        private int outputSize;
        private int expectedSize;
        private byte[] expected;
        private byte[] output;
        private boolean correct;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            Packetizer packetizer = new Packetizer(packetsSize);
            packetizer.setFile(input);

            output = new byte[packetsSize];

            outputSize = packetizer.getNthPacket(nPacket, output, 0);
            correct = (outputSize == expectedSize);
            correct = Arrays.compare(output, 0, expectedSize, expected, 0, expectedSize) == 0;

            if (correct) {
                countCorrect++;
            }
        }

        @Override
        public String toString() {
            String buffer = ((correct == true) ? GREEN_BACKGROUND : RED_BACKGROUND) + "Test " + name() + ((correct == true) ? " -> correct" : " -> wrong") + RESET;

            buffer += "\n\tinput: " + input.toString();
            buffer += "\n\texpected: " + new String(expected);
            buffer += "\n\toutput: " + new String(output);
            buffer += "\n\texpectedSize: " + expectedSize;
            buffer += "\n\toutputSize: " + outputSize;

            return buffer;
        }

        /**
         * @return The expected of the tested function
         */
        public byte[] getOutput() {
            return output;
        }

        /**
         * @return If the executed test was correct, returns true
         */
        public boolean isCorrect() {
            return correct;
        }

        // ##################### static methods #####################
        static public int correctCasesCount() {
            return countCorrect;
        }

        static public boolean allCorrect() {
            return (countCorrect == values().length);
        }

        static public void resumeResults() {
            System.out.printf((allCorrect() ? GREEN_BACKGROUND : RED_BACKGROUND) + "\nCorrect / Total -> %d / %d" + RESET, correctCasesCount(), values().length);

        }

        static public void executeAll() {

            for (TestCase_Packetizer_nPacket test : values()) {
                // Execute the test
                test.execute();

                // Print the test if failed
                if (!test.correct) {

                    System.out.print("\n" + test);
                }

            }
        }

    }

    static enum TestCase_Packetizer_NextPacket {

        _str0_("data/str_file_test.txt", 3, "Exa".getBytes(), "mpl".getBytes()),
        _1_mp3_("data/AliciaKeys-NoOne.mp3", 3, "ID3".getBytes());

        TestCase_Packetizer_NextPacket(String input, int packetsSize, byte[] ... expected) {
            this.input = input;
            this.packetsSize = packetsSize;
            this.expected = expected;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;
        // ##################### properties #####################
        private String input;
        private int packetsSize;
        private byte[][] expected;
        private byte[][] output;
        private boolean correct;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            Packetizer packetizer = new Packetizer(packetsSize);
            packetizer.setFile(input);

            output = new byte[expected.length][packetsSize];

            // for each expected packet, test it, if anyone fails, the test fails
            for (int i = 0; i < expected.length; i++) {
                packetizer.getNextPacket(output[i], 0);
                correct = new String(expected[i]).trim().compareTo(new String(output[i]).trim()) == 0;

                if (!correct) break;
            }

            if (correct) {
                countCorrect++;
            }
        }

        @Override
        public String toString() {
            String buffer = ((correct == true) ? GREEN_BACKGROUND : RED_BACKGROUND) + "Test " + name() + ((correct == true) ? " -> correct" : " -> wrong") + RESET;

            buffer += "\n\tinput: " + input.toString();
            buffer += "\n\texpected packets: ";
            for (byte[] packet : expected) {
                buffer += "\n\t: " + new String(packet);
            }
            buffer += "\n\toutput packets: ";
            for (byte[] packet : output) {
                buffer += "\n\t: " + new String(packet);
            }

            return buffer;
        }

        /**
         * @return The expected of the tested function
         */
        public byte[][] getOutput() {
            return output;
        }

        /**
         * @return If the executed test was correct, returns true
         */
        public boolean isCorrect() {
            return correct;
        }

        // ##################### static methods #####################
        static public int correctCasesCount() {
            return countCorrect;
        }

        static public boolean allCorrect() {
            return (countCorrect == values().length);
        }

        static public void resumeResults() {
            System.out.printf((allCorrect() ? GREEN_BACKGROUND : RED_BACKGROUND) + "\nCorrect / Total -> %d / %d" + RESET, correctCasesCount(), values().length);

        }

        static public void executeAll() {

            for (TestCase_Packetizer_NextPacket test : values()) {
                // Execute the test
                test.execute();

                // Print the test if failed
                if (!test.correct) {

                    System.out.print("\n" + test);
                }

            }
        }

    }


    static enum TestCase_Packetizer_FileFromPacket {

        // this test is verified visualy, looking at the generated files
        _Gstr0_("data/Gstr0.txt", 3, 0, "Exa".getBytes(), "mpl".getBytes()),
        _Gstr1_("data/Gstr1.txt", 3, 1, "<Exa".getBytes(), "<mpl".getBytes());

        TestCase_Packetizer_FileFromPacket(String input, int packetsSize, int off, byte[] ... packets) {
            this.input = input;
            this.off = off;
            this.packetsSize = packetsSize;
            this.packets = packets;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;
        // ##################### properties #####################
        private String input;
        private int off;
        private int packetsSize;
        private byte[][] packets;
        private boolean correct;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            FileFromPacket dePacketizer = null;
            try {

                dePacketizer = new FileFromPacket(input);
            }
            catch (Exception e) {
                System.out.print("\nError Initializing FileFromPacket on test FileFromPacket");
            }

            if (dePacketizer != null) {

                for (byte[] packet : packets) {
                    try {
    
                        dePacketizer.appendPacket(packet, off, packetsSize);
                    }
                    catch (Exception e) {
                        System.out.print("\nError appending packet on test FileFromPacket");
                    }
                }

                try {
    
                    dePacketizer.close();
                }
                catch (Exception e) {
                    System.out.print("\nError closing FileFromPacket on test FileFromPacket");
                }
                
            }
            // output = new byte[expected.length][packetsSize];

            // if (correct) {
            //     countCorrect++;
            // }
        }

        @Override
        public String toString() {

            return "";
        }

        /**
         * @return If the executed test was correct, returns true
         */
        public boolean isCorrect() {
            return correct;
        }

        // ##################### static methods #####################
        static public int correctCasesCount() {
            return countCorrect;
        }

        static public boolean allCorrect() {
            return (countCorrect == values().length);
        }

        static public void resumeResults() {
            System.out.printf((allCorrect() ? GREEN_BACKGROUND : RED_BACKGROUND) + "\nCorrect / Total -> %d / %d" + RESET, correctCasesCount(), values().length);

        }

        static public void executeAll() {

            for (TestCase_Packetizer_FileFromPacket test : values()) {
                // Execute the test
                test.execute();

                // Print the test if failed
                if (!test.correct) {

                    System.out.print("\n" + test);
                }

            }
        }

    }


    // Styling the result of the tests
    public static final String RED_BACKGROUND = "\033[1;30m" + "\033[41m";   // RED
    public static final String GREEN_BACKGROUND = "\033[1;30m" + "\033[42m"; // GREEN
    public static final String GRAY_BACKGROUND = "\033[1;30m" + "\033[37m" + "\033[48;5;235m"; // GRAY

    public static final String RESET = "\033[0m";  // Text format reset

    public static void main(String[] args) {

        // ############################ TestCase_Packetizer_nPacket ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_Packetizer_nPacket" + RESET);
        TestCase_Packetizer_nPacket.executeAll();
        TestCase_Packetizer_nPacket.resumeResults();

        // ############################ TestCase_Packetizer_NextPacket ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_Packetizer_NextPacket" + RESET);
        TestCase_Packetizer_NextPacket.executeAll();
        TestCase_Packetizer_NextPacket.resumeResults();

        // ############################ TestCase_Packetizer_FileFromPacket ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_Packetizer_FileFromPacket" + RESET);
        TestCase_Packetizer_FileFromPacket.executeAll();

        System.out.print("\n");

        
    }
}
