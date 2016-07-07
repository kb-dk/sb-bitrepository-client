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
import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.util.ArgumentValidationUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public class DeleteAction extends RetryingConcurrentClientAction {

    private final String pillarID;
    private final EventHandler eventHandler;
    private DeleteFileClient deleteFileClient;
    
    public DeleteAction(CommandLine cmd, DeleteFileClient deleteFileClient) throws InvalidParameterException {
        super(cmd, new StatusReporter(System.err, Action.DELETE));
        this.deleteFileClient = deleteFileClient;
        pillarID = cmd.getOptionValue(CliOptions.PILLAR_OPT);
        eventHandler = new DeleteFilesEventHandler(super.runningJobs, super.failedJobsQueue, super.reporter);
        ArgumentValidationUtils.validatePillar(pillarID, super.collectionID);
    }

    @Override
    protected void startJob(Job job) throws IOException {
        super.runningJobs.addJob(job);
        job.incrementAttempts();
        deleteFileClient.deleteFile(super.collectionID, job.getRemoteFileID(), pillarID, job.getChecksum(), 
                null, eventHandler, null);
    }

    @Override
    protected Job createJob(String originalFilename, String checksum) throws SkipFileException, 
            MalformedURLException {
        String remoteFilename = FileIDTranslationUtil.localToRemote(originalFilename, super.localPrefix, 
                super.remotePrefix);
        Job job = new Job(Paths.get(originalFilename), remoteFilename, BitmagUtils.getChecksum(checksum), null);
        
        return job;
    }

}
