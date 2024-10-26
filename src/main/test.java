package main;

import song.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {

    static enum TestCase_Song_toRaw {

        _a_(new Song("Despacito", "Jose", "Carlos"), "Despacito:Jose:Carlos");

        TestCase_Song_toRaw(Song input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;

        // ##################### properties #####################
        private Song input;
        private String expected;
        private String output;
        private boolean correct;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            output = input.toRaw();
            correct = output.compareTo(expected) == 0;

            if (correct) {
                countCorrect++;
            }
        }

        @Override
        public String toString() {
            String buffer = ((correct == true) ? GREEN_BACKGROUND : RED_BACKGROUND) + "Test " + name() + ((correct == true) ? " -> correct" : " -> wrong") + RESET;

            buffer += "\n\tinput: " + input.toString();
            buffer += "\n\texpected: " + expected;
            buffer += "\n\toutput: " + output;

            return buffer;
        }

        /**
         * @return The expected of the tested function
         */
        public String getOutput() {
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

            for (TestCase_Song_toRaw test : values()) {
                // Execute the test
                test.execute();

                // Print the test
                System.out.print("\n" + test);

            }
        }

    }

    static enum TestCase_Song_fromRaw {
        _a_("Despacito:Jose:Carlos", new Song("Despacito", "Jose", "Carlos")),
        _b_("Despacito espaciado:Jose:Carlos", new Song("Despacito espaciado", "Jose", "Carlos"));

        TestCase_Song_fromRaw(String input, Song expected) {
            this.input = input;
            this.expected = expected;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;

        // ##################### properties #####################
        private String input;
        private Song expected;
        private Song output;
        private boolean correct = false;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            output = new Song(input);
            correct = expected.equals(output);

            if (correct) {
                countCorrect++;
            }
        }

        @Override
        public String toString() {
            String buffer = ((correct == true) ? GREEN_BACKGROUND : RED_BACKGROUND) + "Test " + name() + ((correct == true) ? " -> correct" : " -> wrong") + RESET;

            buffer += "\n\tinput: " + input;
            buffer += "\n\texpected: " + expected.toString();
            buffer += "\n\toutput: " + output.toString();

            return buffer;
        }

        /**
         * @return The expected of the tested function
         */
        public String getOutput() {
            return output.toString();
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

            for (TestCase_Song_fromRaw test : values()) {
                // Execute the test
                test.execute();

                // Print the test
                System.out.print("\n" + test);

            }
        }

    }

    static enum TestCase_SongList_toRaw {

        _a_(new ArrayList<Song>(Arrays.asList(new Song[]{
            new Song("Despacito", "Carlos", "Jose"),
            new Song("Tomame o dejame", "Naiara")

        })), "Despacito:Carlos:Jose\nTomame o dejame:Naiara");

        TestCase_SongList_toRaw(ArrayList<Song> input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;

        // ##################### properties #####################
        private ArrayList<Song> input;
        private String expected;
        private String output;
        private boolean correct;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            output = SongList.toRaw(input);
            correct = expected.compareTo(output) == 0;

            if (correct) {
                countCorrect++;
            }
        }

        @Override
        public String toString() {
            String buffer = ((correct == true) ? GREEN_BACKGROUND : RED_BACKGROUND) + "Test " + name() + ((correct == true) ? " -> correct" : " -> wrong") + RESET;

            buffer += "\n\tinput: " + input.toString();
            buffer += "\n\texpected: " + expected;
            buffer += "\n\toutput: " + output;

            return buffer;
        }

        /**
         * @return The expected of the tested function
         */
        public String getOutput() {
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

            for (TestCase_SongList_toRaw test : values()) {
                // Execute the test
                test.execute();

                // Print the test
                System.out.print("\n" + test);

            }
        }

    }

    static enum TestCase_SongList_fromRaw {

        _a_("Despacito:Carlos:Jose\nTomame o dejame:Naiara", new ArrayList<Song>(Arrays.asList(new Song[]{
            new Song("Despacito", "Carlos", "Jose"),
            new Song("Tomame o dejame", "Naiara")

        })));

        TestCase_SongList_fromRaw(String input, ArrayList<Song> expected) {
            this.input = input;
            this.expected = expected;
        }

        // ##################### class properties #####################
        static private int countCorrect = 0;

        // ##################### properties #####################
        private String input;
        private ArrayList<Song> expected;
        private ArrayList<Song> output;
        private boolean correct;

        /**
         * @return: True if the test was successful
         */
        public void execute() {
            output = SongList.fromRaw(input);
            correct = SongList.equals(expected, output);

            if (correct) {
                countCorrect++;
            }
        }

        @Override
        public String toString() {
            String buffer = ((correct == true) ? GREEN_BACKGROUND : RED_BACKGROUND) + "Test " + name() + ((correct == true) ? " -> correct" : " -> wrong") + RESET;

            buffer += "\n\tinput: " + input.toString();
            buffer += "\n\texpected: " + expected;
            buffer += "\n\toutput: " + output;

            return buffer;
        }

        /**
         * @return The expected of the tested function
         */
        public ArrayList<Song> getOutput() {
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

            for (TestCase_SongList_fromRaw test : values()) {
                // Execute the test
                test.execute();

                // Print the test
                System.out.print("\n" + test);

            }
        }

    }

    // Styling the result of the tests
    public static final String RED_BACKGROUND = "\033[1;30m" + "\033[41m";   // RED
    public static final String GREEN_BACKGROUND = "\033[1;30m" + "\033[42m"; // GREEN
    public static final String GRAY_BACKGROUND = "\033[1;30m" + "\033[37m" + "\033[48;5;235m"; // GRAY
    
    public static final String RESET = "\033[0m";  // Text format reset

    public static void main(String[] args) {

        // ############################ TestCase_Song_toRaw ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_Song_toRaw" + RESET);
        TestCase_Song_toRaw.executeAll();
        TestCase_Song_toRaw.resumeResults();

        // ############################ TestCase_Song_fromRaw ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_Song_fromRaw" + RESET);
        TestCase_Song_fromRaw.executeAll();
        TestCase_Song_fromRaw.resumeResults();

        // ############################ TestCase_SongList_toRaw ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_SongList_toRaw" + RESET);
        TestCase_SongList_toRaw.executeAll();
        TestCase_SongList_toRaw.resumeResults();

        // ############################ TestCase_SongList_fromRaw ############################
        System.out.print(GRAY_BACKGROUND + "\n\nStarting " + "TestCase_SongList_fromRaw" + RESET);
        TestCase_SongList_fromRaw.executeAll();
        TestCase_SongList_fromRaw.resumeResults();

        System.out.print("\n");
    }
}
