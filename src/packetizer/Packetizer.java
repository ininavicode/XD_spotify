package packetizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Packetizer {
    // ##################### properties #####################
    private final int packetsSize;
    private File loadedFile;
    private FileInputStream fileInputStream;

    // ##################### constructors #####################
    /**
     * Initializes the Packetizer to return packets of the given size, each time the method getNextPacket is called
     */
    public Packetizer(int packetsSize) {
        this.packetsSize = packetsSize;
    }

    /**
     * The method must be called to close the Packetizer and release its resoruces
     */
    public void close() {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                System.out.println("Error closing FileInputStream: " + e.getMessage());
            }
        }
    }

    // ##################### methods #####################
    /**
     * Starts getting the packets from the first byte of the set file.
     */
    public void resetPacketizer() {
        close(); // Close existing stream if open
        if (loadedFile != null) {
            try {
                fileInputStream = new FileInputStream(loadedFile);
            } catch (IOException e) {
                System.out.println("Error opening FileInputStream: " + e.getMessage());
            }
        }
    }

    // ##################### setters #####################
    /**
     * Loads the given filename to the packetizer
     * @throws FileNotFoundException
     */
    public void setFile(String filename) throws FileNotFoundException {
        loadedFile = new File(filename);

        if (!loadedFile.exists()) {
            throw new FileNotFoundException();
        }

        resetPacketizer();
    }

    /**
     * Sets the given "nPacket" to be received with the following getNextPacket
     * @param nPacket
     * @throws IOException
     */
    public void seekPacket(short nPacket) throws IOException {
        // Calculate the byte offset
        long offset = (long) nPacket * packetsSize;
        fileInputStream.getChannel().position(offset);
    }

    // ##################### getters #####################
    /**
     * @return The total packets of "packetsSize" size of the loaded file
     */
    public short getTotalPackets() {
        return (short)(Math.floor(loadedFile.length() / packetsSize) + 1);
    }

    /**
     * Reads the next packet of data from the file.
     * @param packetOutput : Where the data of the packet will be stored.
     * @param off : The initial posistion where the data will be stored at the packetOutput
     * @return The size of the packet (at the end of the file, this may be less than "packetsSize"). Returns -1 if there is no more data in the file.
     */
    public int getNextPacket(byte[] packetOutput, int off) {
        if (fileInputStream == null || packetOutput == null || packetOutput.length < (packetsSize + off)) {
            throw new IllegalArgumentException("Invalid file stream or packetOutput buffer size.");
        }

        int bytesRead = -1;

        try {
            bytesRead = fileInputStream.read(packetOutput, off, packetsSize);
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
            e.printStackTrace();
        }

        return bytesRead;
    }

    /**
     * Reads the nth packet of data from the file, where n is zero-indexed. The cursor of the loaded file is moved
     *  to the next byte of the end of the requested paquet, so be careful when using getNextPacket after this method is used
     * @param n : The packet number to read.
     * @param packetOutput : Where the data of the packet will be stored.
     * @param off : The initial posistion where the data will be stored at the packetOutput
     * @return The size of the packet (at the end of the file, this may be less than "packetsSize"). Returns -1 if there is no more data in the file.
     */
    @Deprecated
    public int getNthPacket(int n, byte[] packetOutput, int off) {
        if (fileInputStream == null || packetOutput == null || packetOutput.length < (packetsSize + off)) {
            throw new IllegalArgumentException("Invalid file stream or packetOutput buffer size.");
        }

        int bytesRead = -1;
        try {
            // Calculate the byte offset
            long offset = (long) n * packetsSize;
            fileInputStream.getChannel().position(offset);
            bytesRead = fileInputStream.read(packetOutput, off, packetsSize);
        } catch (IOException e) {
            System.out.println("An error occurred while seeking/reading the file: " + e.getMessage());
            e.printStackTrace();
        }

        return bytesRead;
    }
}
