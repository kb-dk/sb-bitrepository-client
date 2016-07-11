package dk.statsbiblioteket.bitrepository.commandline.action.job;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Class for holding running PutJobs
 * Makes it possible to limit the number of asynchronous jobs and share job objects between different threads
 */
public class RunningJobs {
    private Map<String, Job> jobs = new ConcurrentHashMap<>(); 
    private Semaphore jobLimiter;

    /**
     * Constructor
     * @param limit The maximum number of concurrent jobs.  
     */
    public RunningJobs(int limit) {
        jobLimiter = new Semaphore(limit);
    }

    /**
     * Will block until the there is room for a new job.
     * @param job The job in the queue.
     */
    public void addJob(Job job) {
        jobLimiter.acquireUninterruptibly();
        jobs.put(job.getRemoteFileID(), job);

    }
    
    /**
     * Gets the PutJob for fileID
     * @param fileID The fileID to get the job for
     * @return PutJob the PutJob with relevant info for the job. May return null if no job matching fileID is found
     */
    public Job getJob(String fileID) {
        return jobs.get(fileID);
    }

    /**
     * Removes a job from the queue
     * @param job the PutJob to remove 
     */
    public void removeJob(Job job) {
        Job removedJob = jobs.remove(job.getRemoteFileID());
        if(removedJob != null) {
            jobLimiter.release();
        }
    }
    
    /**
     * Determine if there are no more jobs in the queue 
     * @return true if there are no more jobs, otherwise false
     */
     public boolean isEmpty() {
        return jobs.isEmpty();
    }
}
