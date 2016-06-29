package dk.statsbiblioteket.bitrepository.commandline.action.upload;

import java.net.URL;
import java.nio.file.Path;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;

public class PutJob {

    private int putAttempts = 0;
    private Path localFile;
    private String remoteFileID;
    private ChecksumDataForFileTYPE checksum;
    private URL url;
    
    public PutJob(Path localFile, String remoteFileID, ChecksumDataForFileTYPE checksum, URL url) {
        this.localFile = localFile;
        this.remoteFileID = remoteFileID;
        this.checksum = checksum;
        this.url = url;
    }

    public Path getLocalFile() {
        return localFile;
    }

    public String getRemoteFileID() {
        return remoteFileID;
    }

    public ChecksumDataForFileTYPE getChecksum() {
        return checksum;
    }

    public URL getUrl() {
        return url;
    }

    public int getPutAttempts() {
        return putAttempts;
    }
    
    public void incrementPutAttempts() {
        putAttempts++;
    }
}
