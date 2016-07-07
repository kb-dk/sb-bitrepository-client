package dk.statsbiblioteket.bitrepository.commandline.action.job;

import java.net.URL;
import java.nio.file.Path;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;

/**
 * Interface describing the minimum needed for a Job 
 */
public class Job {
    
    private int attempts = 0;
    private Path localFile;
    private String remoteFileID;
    private ChecksumDataForFileTYPE checksum;
    private URL url;
    
    public Job(Path localFile, String remoteFileID, ChecksumDataForFileTYPE checksum, URL url) {
        this.localFile = localFile;
        this.remoteFileID = remoteFileID;
        this.checksum = checksum;
        this.url = url;
    }

    /**
     * Gets the path to the local file 
     */
    public Path getLocalFile() {
        return localFile;
    }

    /**
     * Gets the id of the file on the remote side.  
     */
    public String getRemoteFileID() {
        return remoteFileID;
    }

    /**
     * Gets the bitrepository.org datastructure for the checksum for the job. 
     * @return {@link ChecksumDataForFileTYPE} The checksum structure.  
     */
    public ChecksumDataForFileTYPE getChecksum() {
        return checksum;
    }

    /**
     * Gets the URL for file exchange for the job. 
     * @return URL The url 
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Gets the number of attempts that the job have been tried.
     * @return The number of attempts  
     */
    public int getAttempts() {
        return attempts;
    }
    
    /**
     * Increments the number of attempts the job have been tried.  
     */
    public void incrementAttempts() {
        attempts++;
    }

}
