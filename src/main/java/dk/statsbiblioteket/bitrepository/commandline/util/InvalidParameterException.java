package dk.statsbiblioteket.bitrepository.commandline.util;

/**
 * Exception class to tell that a parameters value has been deemed invalid.  
 */
public class InvalidParameterException extends Exception {

    /**
     * Constructor
     * @param message The message detailing why the parameter validation failed.  
     */
    public InvalidParameterException(String message) {
        super(message);
    }
    
}
