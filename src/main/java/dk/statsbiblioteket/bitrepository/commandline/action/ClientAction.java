package dk.statsbiblioteket.bitrepository.commandline.action;

/**
 * Simple interface for grouping the various actions that the client can perfom
 */
public interface ClientAction {

    /**
     * Method to perform the implemented action 
     */
    void performAction();
}
