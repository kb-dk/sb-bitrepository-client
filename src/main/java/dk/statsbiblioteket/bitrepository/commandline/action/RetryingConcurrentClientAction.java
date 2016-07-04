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

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;
import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.ArgumentValidationUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public abstract class RetryingConcurrentClientAction<T extends Job> implements ClientAction {

    protected int asyncJobs;
    protected int maxRetries;
    protected String localPrefix = null;
    protected String remotePrefix = null;
    protected String collectionID;
    protected Path sumFile;
    protected Action clientAction;
    protected RunningJobs<T> runningJobs;
    protected final BlockingQueue<T> failedJobsQueue = new LinkedBlockingQueue<>();
    protected StatusReporter reporter = new StatusReporter(System.err);
    
    public RetryingConcurrentClientAction(CommandLine cmd) throws InvalidParameterException {
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        localPrefix = cmd.hasOption(CliOptions.LOCAL_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT) : null;
        remotePrefix = cmd.hasOption(CliOptions.REMOTE_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT) : null;
        maxRetries = cmd.hasOption(CliOptions.RETRY_OPT) ? Integer.parseInt(cmd.getOptionValue(CliOptions.RETRY_OPT)) : 1;
        asyncJobs = cmd.hasOption(CliOptions.ASYNC_OPT) ? Integer.parseInt(cmd.getOptionValue(CliOptions.ASYNC_OPT)) : 1;
        runningJobs = new RunningJobs<>(asyncJobs);
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
                    reporter.reportSkipFile(clientAction, origFilename);
                    continue;
                }
                startJob(job);
                reporter.reportStart(clientAction, origFilename);
            }
            
            while(!finished()) {
                retryFailedJobs();
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        } catch (InterruptedException e) {
            System.err.format("InterruptedException: %s%n", e);
        }
    }
    
    
    protected void retryFailedJobs() throws IOException {
        Set<T> jobs = new HashSet<>();
        failedJobsQueue.drainTo(jobs);
        for(T job : jobs) {
            if(job.getAttempts() < maxRetries) {
                startJob(job);
            } else {
                reporter.reportFailure(clientAction, job.getLocalFile().toString());
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
