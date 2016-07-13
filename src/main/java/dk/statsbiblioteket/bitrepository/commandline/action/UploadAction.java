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

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.upload.PutFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * Action for handling upload of files to the repository. 
 * The class uses the functionality of {@link RetryingConcurrentClientAction} to handle 
 * reading sumfile containing which files to process, concurrency and retry logic.
 * Files that are already uploaded are omitted by utilizing the bitrepository clients build in idempotence. 
 */
public class UploadAction extends RetryingConcurrentClientAction {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private PutFileClient putFileClient;
    private EventHandler eventHandler;
    
    /**
     * Constructor for the action
     * @param cmd The {@link CommandLine} with parsed arguments
     * @param putFileClient The {@link PutFileClient} to put the files in the repository
     * @param fileExchange The {@link FileExchange} used to transfer files between client and repository
     * @param reporter The {@link StatusReporter} to report status for processed files 
     * @throws InvalidParameterException if input fails validation
     */
    public UploadAction(CommandLine cmd, PutFileClient putFileClient, FileExchange fileExchange, StatusReporter reporter) 
            throws InvalidParameterException {
        super(cmd, reporter);
        this.putFileClient = putFileClient;
        eventHandler = new PutFilesEventHandler(fileExchange, super.runningJobs, super.failedJobsQueue, super.reporter);
    }
    
    @Override
    protected Job createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException {
        String remoteFilename = FileIDTranslationUtil.localToRemote(originalFilename, super.localPrefix, 
                super.remotePrefix);
        Job job = new Job(Paths.get(originalFilename), remoteFilename, BitmagUtils.getChecksum(checksum), 
                getUrl(remoteFilename));
        return job;
    }
    
    @Override
    protected void startJob(Job job) {
        try {
            putFileClient.putFile(super.collectionID, job.getUrl(), job.getRemoteFileID(), Files.size(job.getLocalFile()), 
                    job.getChecksum(), null, eventHandler, null);
        } catch (IOException e) {
            log.error("Could not get filesize for file '{}'", job.getLocalFile(), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method to create the URL to where the file should be placed on the {@link FileExchange}. 
     * The URL's should be unique, but reproducible so as to help keep the {@link FileExchange} clean.   
     */
    private URL getUrl(String filename) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        String path = DigestUtils.md5Hex(super.collectionID + filename);
        return new URL(baseurl.toString() + path);
    }

}
