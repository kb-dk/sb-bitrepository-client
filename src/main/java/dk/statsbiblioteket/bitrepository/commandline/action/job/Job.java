package dk.statsbiblioteket.bitrepository.commandline.action.job;

import java.nio.file.Path;

/**
 * Interface describing the minimum needed for a Job 
 */
public interface Job {
    
    /**
     * Gets the id of the file on the remote side.  
     */
    String getRemoteFileID();
    
    /**
     * Gets the number of attempts that the job have been tried.  
     */
    int getAttempts(); 
    
    /**
     * Increments the number of attempts the job have been tried.  
     */
    void incrementAttempts();
    
    /**
     * Gets the path to the local file 
     */
    public Path getLocalFile();

}
