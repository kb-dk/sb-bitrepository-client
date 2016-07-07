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
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public abstract class RetryingConcurrentClientAction<T extends Job> implements ClientAction {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected int maxRetries;
    protected String localPrefix = null;
    protected String remotePrefix = null;
    protected String collectionID;
    protected Path sumFile;
    protected RunningJobs<T> runningJobs;
    protected final BlockingQueue<T> failedJobsQueue = new LinkedBlockingQueue<>();
    protected StatusReporter reporter;
    
    public RetryingConcurrentClientAction(CommandLine cmd, StatusReporter reporter) throws InvalidParameterException {
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        localPrefix = cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT);
        remotePrefix = cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT);
        maxRetries = Integer.parseInt(cmd.getOptionValue(CliOptions.RETRY_OPT, "1"));
        int asyncJobs = Integer.parseInt(cmd.getOptionValue(CliOptions.ASYNC_OPT, "1"));
        runningJobs = new RunningJobs<>(asyncJobs);
        this.reporter = reporter;
        ArgumentValidationUtils.validateCollection(collectionID);
    }
    
    @Override
    public void performAction() {
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(sumFile, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("  ");
                String origFilename = parts[1];
                String checksum = parts[0];
                T job;
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
    
    
    protected void retryFailedJobs() throws IOException {
        Set<T> jobs = new HashSet<>();
        failedJobsQueue.drainTo(jobs);
        for(T job : jobs) {
            if(job.getAttempts() < maxRetries) {
                startJob(job);
            } else {
                reporter.reportFailure(job.getLocalFile().toString());
            }
        }
    }
    
    protected boolean finished() {
        /* Ye be warned, the sequence of the '&&' matters.*/
        return (runningJobs.isEmpty() && failedJobsQueue.isEmpty());
    }
    
    protected abstract void startJob(T job) throws IOException;
    
    protected abstract T createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException;

}
