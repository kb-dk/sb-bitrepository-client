package dk.statsbiblioteket.bitrepository.commandline.util;

/**
 * Exception class to tell that a parameters value has been deemed invalid.  
 */
public class InvalidParameterException extends Exception {

    InvalidParameterException(String message) {
        super(message);
    }
    
}
