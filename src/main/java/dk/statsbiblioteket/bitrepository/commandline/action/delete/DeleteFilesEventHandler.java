package dk.statsbiblioteket.bitrepository.commandline.action.delete;

import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * EventHandler to handle events related to DeleteFile operations.
 * Jobs that are failed are queued. Completed jobs reported completed.  
 */
public class DeleteFilesEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RunningJobs runningJobs;
    private final BlockingQueue<Job> failedJobsQueue;
    private final StatusReporter reporter;
    
    /**
     * Constructor
     * @param runningJobs {@link RunningJobs} object for containing active jobs
     * @param failedJobsQueue {@link BlockingQueue} for jobs that have failed
     * @param reporter {@link StatusReporter} to report succeded jobs. 
     */
    public DeleteFilesEventHandler(RunningJobs runningJobs, BlockingQueue<Job> failedJobsQueue,
            StatusReporter reporter) {
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
                log.info("Finished delete file for file '{}'", event.getFileID());
                reporter.reportFinish(job.getLocalFile().toString());
                runningJobs.removeJob(job);
                break;
            case FAILED:
                log.warn("Failed delete file for file '{}'", event.getFileID());
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
   
}
