package dk.statsbiblioteket.bitrepository.commandline.action.delete;

import java.nio.file.Path;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;

public class DeleteJob implements Job {

    private int attempts = 0;
    private final String remoteFileID;
    private final ChecksumDataForFileTYPE checksum;
    private Path localFile;

    public DeleteJob(Path localFile, String remoteFileID, ChecksumDataForFileTYPE checksum) {
        this.localFile = localFile;
        this.remoteFileID = remoteFileID;
        this.checksum = checksum;
    }
    
    @Override
    public String getRemoteFileID() {
        return remoteFileID;
    }
    
    public ChecksumDataForFileTYPE getChecksum() {
        return checksum;
    }

    @Override
    public int getAttempts() {
        return attempts;
    }

    @Override
    public void incrementAttempts() {
        attempts++;
    }

    @Override
    public Path getLocalFile() {
        return localFile;
    }

    

}
