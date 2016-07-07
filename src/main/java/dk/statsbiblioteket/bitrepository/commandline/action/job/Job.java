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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + attempts;
        result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
        result = prime * result + ((localFile == null) ? 0 : localFile.hashCode());
        result = prime * result + ((remoteFileID == null) ? 0 : remoteFileID.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Job other = (Job) obj;
        if (attempts != other.attempts)
            return false;
        if (checksum == null) {
            if (other.checksum != null)
                return false;
        } else if (!checksum.equals(other.checksum))
            return false;
        if (localFile == null) {
            if (other.localFile != null)
                return false;
        } else if (!localFile.equals(other.localFile))
            return false;
        if (remoteFileID == null) {
            if (other.remoteFileID != null)
                return false;
        } else if (!remoteFileID.equals(other.remoteFileID))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

}
