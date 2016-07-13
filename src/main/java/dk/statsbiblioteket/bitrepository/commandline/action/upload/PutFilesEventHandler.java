package dk.statsbiblioteket.bitrepository.commandline.action.upload;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.protocol.FileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * Event handler class to handle uploads of files. 
 * The class processes incoming events and:
 * - Uploads files to the {@link FileExchange} when a PutFile operation requires it
 * - Deletes files from the {@link FileExchange} when they are no longer needed
 * - Reports the operation status for the {@link Job}'s, i.e. successful or failed. 
 */
public class PutFilesEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FileExchange fileExchange;
    private final RunningJobs runningJobs;
    private final BlockingQueue<Job> failedJobsQueue;
    private final StatusReporter reporter;
    
    /**
     * Constructor for the handler
     * @param fileExchange The {@link FileExchange} used in the operations
     * @param runningJobs The {@link RunningJobs} object with active operations
     * @param failedJobsQueue {@link BlockingQueue} for communicating failed jobs
     * @param reporter {@link StatusReporter} for reporting succeeded jobs 
     */
    public PutFilesEventHandler(FileExchange fileExchange, RunningJobs runningJobs, 
            BlockingQueue<Job> failedJobsQueue, StatusReporter reporter) {
        this.fileExchange = fileExchange;
        this.runningJobs = runningJobs;
        this.failedJobsQueue = failedJobsQueue;
        this.reporter = reporter;
    }

    @Override
    public void handleEvent(OperationEvent event) {
        Job job = runningJobs.getJob(event.getFileID());
        if(job != null) {
            switch(event.getEventType()) {
            case IDENTIFICATION_COMPLETE:
                IdentificationCompleteEvent ICEvent = (IdentificationCompleteEvent) event;
                if(!ICEvent.getContributorIDs().isEmpty()) {
                    log.debug("Finished identification for put file. File '{}' is needed on {} pillars. Uploading file.", 
                            ICEvent.getFileID(), ICEvent.getContributorIDs().size());
                    uploadFile(job);
                } else {
                    log.info("Finished identification for put file. File '{}' is already on all pillars.", ICEvent.getFileID());
                }
                break;
            case COMPLETE:
                log.info("Finished put file for file '{}'", event.getFileID());
                reporter.reportFinish(job.getLocalFile().toString());
                removeFileFromFileExchange(job);
                runningJobs.removeJob(job);
                break;
            case FAILED:
                log.warn("Failed put file for file '{}'", event.getFileID());
                failJob(job);
                break;
            default:
                break;
            }    
        } else {
            log.warn("Got an event for a job that can not be found. The job was for fileID: '{}'", event.getFileID());
        }
    }
    
    /**
     * Method to handle a bookkeeping and clean-up of a failed job. 
     * @param job The job that is to be marked as failed.  
     */
    private void failJob(Job job) {
        removeFileFromFileExchange(job);
        failedJobsQueue.add(job);
        runningJobs.removeJob(job);
    }
    
    /**
     * Method to handle the upload of a file in a job to the {@link FileExchange}
     * @param job The job to upload the file for 
     */
    private void uploadFile(Job job) {
        try {
            fileExchange.putFile(Files.newInputStream(job.getLocalFile()), job.getUrl());
            log.debug("Finished upload of file {}.", job.getRemoteFileID());
        } catch (IOException e) {
            log.error("Failed to upload file {}.", job.getRemoteFileID(), e);
            failJob(job);
        }
    }
    
    /**
     * Method to remove the file for a job from the {@link FileExchange}
     * @param job The job to remove the file for
     */
    private void removeFileFromFileExchange(Job job) {
        try {
            fileExchange.deleteFile(job.getUrl());
            log.debug("Finished removing file {} from file exchange.", job.getUrl());
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to remove file '{}' from file exchange.", job.getUrl(), e);
        }
    }

}
