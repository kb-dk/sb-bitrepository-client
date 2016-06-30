package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.digest.DigestUtils;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.action.upload.PutFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.action.upload.PutJob;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public class UploadAction implements ClientAction {

    private final Logger log = LoggerFactory.getLogger(getClass());
        
    private final String collectionID;
    private final Path sumFile;
    private final int asyncJobs;
    private final int maxRetries;
    private String localPrefix = null;
    private String remotePrefix = null;
    private PutFileClient putFileClient;
    private RunningJobs runningJobs;
    private final BlockingQueue<PutJob> failedJobsQueue = new LinkedBlockingQueue<>();
    private StatusReporter reporter = new StatusReporter(System.err);
    private EventHandler eventHandler;
    
    public UploadAction(CommandLine cmd, PutFileClient putFileClient, FileExchange fileExchange) {
        this.putFileClient = putFileClient;
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        localPrefix = cmd.hasOption(CliOptions.LOCAL_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT) : null;
        remotePrefix = cmd.hasOption(CliOptions.REMOTE_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT) : null;
        maxRetries = cmd.hasOption(CliOptions.RETRY_OPT) ? Integer.parseInt(cmd.getOptionValue(CliOptions.RETRY_OPT)) : 1;
        asyncJobs = cmd.hasOption(CliOptions.ASYNC_OPT) ? Integer.parseInt(cmd.getOptionValue(CliOptions.ASYNC_OPT)) : 1;
        runningJobs = new RunningJobs(asyncJobs);
        eventHandler = new PutFilesEventHandler(fileExchange, runningJobs, failedJobsQueue, reporter);
    }
    
    public void performAction() {
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(sumFile, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("  ");
                String origFilename = parts[1];
                String checksum = parts[0];
                
                String remoteFilename;
                try {
                    remoteFilename = FileIDTranslationUtil.localToRemote(origFilename, localPrefix, remotePrefix);
                } catch (SkipFileException e) {
                    reporter.reportSkipFile(Action.UPLOAD, origFilename);
                    continue;
                }
                
                PutJob job = new PutJob(Paths.get(origFilename), remoteFilename, BitmagUtils.getChecksum(checksum), 
                        getUrl(remoteFilename));
                startPutJob(job);
                reporter.reportStart(Action.UPLOAD, origFilename);
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
    
    private void retryFailedJobs() throws IOException {
        Set<PutJob> jobs = new HashSet<>();
        failedJobsQueue.drainTo(jobs);
        for(PutJob job : jobs) {
            if(job.getPutAttempts() < maxRetries) {
                startPutJob(job);
            } else {
                reporter.reportFailure(Action.UPLOAD, job.getLocalFile().toString());
            }
        }
    }
    
    private boolean finished() {
        /* Ye be warned, the sequence of the '&&' matters.*/
        return (runningJobs.isEmpty() && failedJobsQueue.isEmpty());
    }
    
    private void startPutJob(PutJob job) throws IOException {
        runningJobs.addJob(job);
        job.incrementPutAttempts();
        putFileClient.putFile(collectionID, job.getUrl(), job.getRemoteFileID(), Files.size(job.getLocalFile()), 
                job.getChecksum(), null, eventHandler, null);
    }
    
    private URL getUrl(String filename) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        String path = DigestUtils.md5Hex(collectionID + filename);
        return new URL(baseurl.toString() + path);
    }

}
