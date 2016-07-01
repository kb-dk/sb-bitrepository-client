package dk.statsbiblioteket.bitrepository.commandline.action.download;

import java.net.URL;
import java.nio.file.Path;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;

public class DownloadJob implements Job {

    private int attempts = 0;
    private Path localFile;
    private String remoteFileID;
    private ChecksumDataForFileTYPE checksum;
    private URL url;
    
    public DownloadJob(Path localFile, String remoteFileID, ChecksumDataForFileTYPE checksum, URL url) {
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

    public int getAttempts() {
        return attempts;
    }
    
    public void incrementAttempts() {
        attempts++;
    }
}
