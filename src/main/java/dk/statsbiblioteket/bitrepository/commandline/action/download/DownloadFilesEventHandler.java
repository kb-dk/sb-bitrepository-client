package dk.statsbiblioteket.bitrepository.commandline.action.download;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.protocol.FileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * EventHandler to handle events related to GetFile operations.
 * Jobs that fail are queued for retried. When a download job finishes, 
 * the downloaded file will be reported to the status reporter as completed, 
 * and removed from the file exchange.  
 */
public class DownloadFilesEventHandler implements EventHandler {
    
    protected final static String TEMP_EXTENSION = ".tmp";
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FileExchange fileExchange;
    private final RunningJobs runningJobs;
    private final BlockingQueue<Job> failedJobsQueue;
    private final StatusReporter reporter;
    
    public DownloadFilesEventHandler(FileExchange fileExchange, RunningJobs runningJobs, 
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
            case COMPLETE:
                log.info("Finished get file for file '{}'", event.getFileID());
                boolean success = downloadFile(job);
                if(success) {
                    reporter.reportFinish(job.getLocalFile().toString());
                    runningJobs.removeJob(job);
                } else {
                    failedJobsQueue.add(job);
                    runningJobs.removeJob(job);
                }
                removeFileFromFileExchange(job);
                break;
            case FAILED:
                log.warn("Failed get file for file '{}'", event.getFileID());
                removeFileFromFileExchange(job);
                failedJobsQueue.add(job);
                runningJobs.removeJob(job);
                break;
            default:
                break;
            }    
        } else {
            log.warn("Got an event for a job that can not be found. The job was for fileID: '{}'", event.getFileID());
        }
    }
    
    /**
     * Get the file from the file exchange
     * @param job The job to get the file for
     * @return boolean, true if the file was successfully obtained from the fileexchange, otherwise false.
     */
    private boolean downloadFile(Job job) {
        try {
            Path local = job.getLocalFile();
            Path temp = local.resolveSibling(local.getFileName() + TEMP_EXTENSION);
            Path tempParent = temp.getParent();
            if(tempParent != null) {
                Files.createDirectories(tempParent);    
            }   
            fileExchange.getFile(Files.newOutputStream(temp), job.getUrl());
            Files.move(temp, local);
            log.debug("Finished download of file {}.", job.getRemoteFileID());
            return true;
        } catch (IOException e) {
            log.error("Failed to download file {}.", job.getRemoteFileID(), e);
            return false;
        }
    }
    
    /**
     * Method to remove the file from the file exchange
     * @param job The job to remove the file for.  
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
