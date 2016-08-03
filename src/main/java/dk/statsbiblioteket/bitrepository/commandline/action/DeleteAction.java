package dk.statsbiblioteket.bitrepository.commandline.action;

import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.deletefile.DeleteFileClient;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.action.delete.DeleteFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.util.ArgumentValidationUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * Action handling deletion of files on a specific pillar. 
 * The class uses the functionality of {@link RetryingConcurrentClientAction} to handle 
 * reading sumfile containing which files to process, concurrency and retry logic.  
 */
public class DeleteAction extends RetryingConcurrentClientAction {

    private final String pillarID;
    private final EventHandler eventHandler;
    private DeleteFileClient deleteFileClient;
    
    /**
     * Constructor for the action
     * @param cmd The {@link CommandLine} with parsed arguments
     * @param deleteFileClient The {@link DeleteFileClient} to be used to delete files with 
     * @param reporter The {@link StatusReporter} to report status for processed files
     * @throws InvalidParameterException if input fails validation
     */
    public DeleteAction(CommandLine cmd, DeleteFileClient deleteFileClient, StatusReporter reporter) 
            throws InvalidParameterException {
        super(cmd, reporter);
        this.deleteFileClient = deleteFileClient;
        pillarID = cmd.getOptionValue(CliOptions.PILLAR_OPT);
        eventHandler = new DeleteFilesEventHandler(super.runningJobs, super.failedJobsQueue, super.reporter);
        ArgumentValidationUtils.validatePillar(pillarID, super.collectionID);
    }

    @Override
    protected void runJob(Job job) {
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
