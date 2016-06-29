package dk.statsbiblioteket.bitrepository.commandline.util;

/**
 * Exception class to indicate a file should be skipped from processing 
 */
public class SkipFileException extends Exception {

    /**
     * Constructor
     * @param message The message 
     */
    public SkipFileException(String message) {
        super(message);
    }
}
