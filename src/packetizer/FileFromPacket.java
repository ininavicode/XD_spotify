package packetizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileFromPacket {

    // ##################### properties #####################
    private File file;
    private FileOutputStream outputStream;

    // ##################### constructors #####################
    /**
     * Initializes the Mp3FromPackets instance with a specified filename, which will be created if it doesn’t exist.
     * If the file already exists, it will be overwritten.
     * @param filename The name of the file to be created and written to.
     * @throws IOException if an error occurs while creating or opening the file.
     */
    public FileFromPacket(String filename) throws IOException {
        file = new File(filename);
        
        // If file already exists, overwrite it
        if (file.exists()) {
            file.delete();
        }
        
        file.createNewFile(); // Create a new, empty file
        outputStream = new FileOutputStream(file, true); // Set to append mode
    }

    // ##################### methods #####################
    /**
     * Appends a packet to the file.
     * @param packet The byte array containing the data to append.
     * @param off The initial posistion where the data will be read from the packet
     * @param length The number of bytes from the packet array to write to the file.
     * @throws IOException if an error occurs during file writing.
     */
    public void appendPacket(byte[] packet, int off, int length) throws IOException {
        if (outputStream == null || packet.length < (off + length)) {
            throw new IllegalArgumentException("Invalid file stream or packetOutput buffer size.");
        }
        outputStream.write(packet, off, length);
        
    }

    /**
     * Closes the FileOutputStream, releasing any system resources associated with it.
     * This should be called when the file is no longer needed to ensure data is saved.
     * @throws IOException if an error occurs while closing the stream.
     */
    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
    }

}
