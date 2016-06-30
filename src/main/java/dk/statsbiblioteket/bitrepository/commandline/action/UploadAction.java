package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class UploadAction extends RetryingConcurrentClientAction<PutJob> implements ClientAction {

    private final Logger log = LoggerFactory.getLogger(getClass());
        
    private final String collectionID;
    private PutFileClient putFileClient;
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
        runningJobs = new RunningJobs<>(asyncJobs);
        eventHandler = new PutFilesEventHandler(fileExchange, runningJobs, failedJobsQueue, reporter);
        clientAction = Action.UPLOAD;
    }
    
    protected PutJob createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException {
        String remoteFilename = FileIDTranslationUtil.localToRemote(originalFilename, localPrefix, remotePrefix);
        PutJob job = new PutJob(Paths.get(originalFilename), remoteFilename, BitmagUtils.getChecksum(checksum), 
                getUrl(remoteFilename));
        return job;
    }
    
    protected void startJob(PutJob job) throws IOException {
        runningJobs.addJob(job);
        job.incrementAttempts();
        putFileClient.putFile(collectionID, job.getUrl(), job.getRemoteFileID(), Files.size(job.getLocalFile()), 
                job.getChecksum(), null, eventHandler, null);
    }
    
    private URL getUrl(String filename) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        String path = DigestUtils.md5Hex(collectionID + filename);
        return new URL(baseurl.toString() + path);
    }

}
