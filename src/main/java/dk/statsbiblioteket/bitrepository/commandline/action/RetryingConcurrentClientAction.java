package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.ArgumentValidationUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * Abstract class to gather the common parts for actions that:
 * - Reads a sumfile and performs an action each file. 
 * - Needs filtering capability to ommit or translate files between local and remote
 * - Needs support for retrying operations/jobs
 * - Needs support for running concurrent operations/jobs
 */
public abstract class RetryingConcurrentClientAction implements ClientAction {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected int maxRetries;
    protected String localPrefix = null;
    protected String remotePrefix = null;
    protected String collectionID;
    protected Path sumFile;
    protected RunningJobs runningJobs;
    protected final BlockingQueue<Job> failedJobsQueue = new LinkedBlockingQueue<>();
    protected StatusReporter reporter;
    
    /**
     * Constructor 
     * @param cmd The {@link CommandLine} with parsed arguments
     * @param reporter The {@link StatusReporter} to report status for processed files 
     * @throws InvalidParameterException if input fails validation
     */
    public RetryingConcurrentClientAction(CommandLine cmd, StatusReporter reporter) throws InvalidParameterException {
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        localPrefix = cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT);
        remotePrefix = cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT);
        maxRetries = Integer.parseInt(cmd.getOptionValue(CliOptions.RETRY_OPT, "1"));
        int asyncJobs = Integer.parseInt(cmd.getOptionValue(CliOptions.ASYNC_OPT, "1"));
        runningJobs = new RunningJobs(asyncJobs);
        this.reporter = reporter;
        ArgumentValidationUtils.validateCollection(collectionID);
    }
    
    @Override
    public void performAction() {
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(sumFile, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(MD5SumFileWriter.MD5_FILE_FIELD_SEPERATOR);
                String checksum = parts[0];
                String origFilename = line.substring(checksum.length() + MD5SumFileWriter.MD5_FILE_FIELD_SEPERATOR.length());
                Job job;
                try {
                    job = createJob(origFilename, checksum);
                } catch (SkipFileException e) {
                    reporter.reportSkipFile(origFilename);
                    continue;
                }
                startJob(job);
                reporter.reportStart(origFilename);
            }
            
            while(!finished()) {
                retryFailedJobs();
                Thread.sleep(1000);
            }
            reporter.printStatistics();
        } catch (IOException e) {
            log.error("Caught IOException while processing sumfile", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for finish", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method to handle the re-scheduling of failed jobs, or to report them as failed
     * if the maximum number of attempts have been tried.  
     */
    protected void retryFailedJobs(){
        Set<Job> jobs = new HashSet<>();
        failedJobsQueue.drainTo(jobs);
        for(Job job : jobs) {
            if(job.getAttempts() < maxRetries) {
                startJob(job);
            } else {
                reporter.reportFailure(job.getLocalFile().toString());
            }
        }
    }
    
    /**
     * Method to do the bookkeeping of starting a job, i.e. adding it to the running jobs queue, 
     * incrementing the attempts it has been started and delegating the actual task of running the 
     * job to the runJob method. 
     * @param job The {@link Job} to be started. 
     */
    private void startJob(Job job) {
        runningJobs.addJob(job);
        job.incrementAttempts();
        runJob(job);
    }
    
    /**
     * Method to determine if processing is finished. I.e. there is no more running jobs
     * and no jobs potentially needs to be retried. 
     * @return true if there are no running jobs an the queue with failed jobs is empty, 
     *          otherwise false
     */
    protected boolean finished() {
        /* Ye be warned, the sequence of the '&&' matters.*/
        return (runningJobs.isEmpty() && failedJobsQueue.isEmpty());
    }
    
    
    /**
     * Method to run a job for a specific action type.
     * @param job The {@link Job} to be run 
     */
    protected abstract void runJob(Job job);
    
    /**
     * Method to create a job for a specific action type. 
     * @param originalFilename The filename as it is on the local machine.
     * @param checksum The checksum for the file
     */
    protected abstract Job createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException;

}
