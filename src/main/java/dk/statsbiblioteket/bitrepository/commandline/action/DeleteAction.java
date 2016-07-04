package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.deletefile.DeleteFileClient;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;
import dk.statsbiblioteket.bitrepository.commandline.action.delete.DeleteFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.action.delete.DeleteJob;
import dk.statsbiblioteket.bitrepository.commandline.util.ArgumentValidationUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public class DeleteAction extends RetryingConcurrentClientAction<DeleteJob> implements ClientAction {

    private final String pillarID;
    private final EventHandler eventHandler;
    private DeleteFileClient deleteFileClient;
    
    public DeleteAction(CommandLine cmd, DeleteFileClient deleteFileClient) throws InvalidParameterException {
        super(cmd, new StatusReporter(System.err, Action.DELETE));
        this.deleteFileClient = deleteFileClient;
        pillarID = cmd.getOptionValue(CliOptions.PILLAR_OPT);
        eventHandler = new DeleteFilesEventHandler(runningJobs, super.failedJobsQueue, super.reporter);
        ArgumentValidationUtils.validatePillar(pillarID, collectionID);
    }

    @Override
    protected void startJob(DeleteJob job) throws IOException {
        runningJobs.addJob(job);
        job.incrementAttempts();
        deleteFileClient.deleteFile(collectionID, job.getRemoteFileID(), pillarID,job.getChecksum(), 
                null, eventHandler, null);
    }

    @Override
    protected DeleteJob createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException {
        String remoteFilename = FileIDTranslationUtil.localToRemote(originalFilename, localPrefix, remotePrefix);
        DeleteJob job = new DeleteJob(Paths.get(originalFilename), remoteFilename, BitmagUtils.getChecksum(checksum));
        
        return job;
    }

}
