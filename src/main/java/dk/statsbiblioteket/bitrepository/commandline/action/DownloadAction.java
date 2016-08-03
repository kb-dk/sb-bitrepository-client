package dk.statsbiblioteket.bitrepository.commandline.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.digest.DigestUtils;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.protocol.FileExchange;

import dk.statsbiblioteket.bitrepository.commandline.action.download.DownloadFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * Action for handling download of files from the repository. 
 * The class uses the functionality of {@link RetryingConcurrentClientAction} to handle 
 * reading sumfile containing which files to process, concurrency and retry logic.
 * Files that are already downloaded (or have naming collisions) are explicitly omitted from download. 
 * The action class chooses the pillar which is deemed fastest by the {@link GetFileClient}
 */
public class DownloadAction extends RetryingConcurrentClientAction {

    private GetFileClient getFileClient;
    private EventHandler eventHandler;

    /**
     * Constructor for the action
     * @param cmd The {@link CommandLine} with parsed arguments
     * @param getFileClient The {@link GetFileClient} to get files from the repository
     * @param fileExchange The {@link FileExchange} used to transfer files between repository and client
     * @param reporter The {@link StatusReporter} to report status for processed files 
     * @throws InvalidParameterException if input fails validation
     */
    public DownloadAction(CommandLine cmd, GetFileClient getFileClient, FileExchange fileExchange, 
            StatusReporter reporter) throws InvalidParameterException {
        super(cmd, reporter);
        this.getFileClient = getFileClient;
        eventHandler = new DownloadFilesEventHandler(fileExchange, super.runningJobs, super.failedJobsQueue, 
                super.reporter);
    }
    
    @Override
    protected void runJob(Job job) {
        getFileClient.getFileFromFastestPillar(super.collectionID, job.getRemoteFileID(), null, job.getUrl(), 
                eventHandler, null);
    }

    @Override
    protected Job createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException {
        Path localFile = Paths.get(originalFilename);
        // Figure out how to handle file with prefixes when downloading..
        if(Files.exists(localFile)) {
            throw new SkipFileException("Skipping file as it already exists");
        }
        String remoteFilename = FileIDTranslationUtil.localToRemote(originalFilename, super.localPrefix, 
                super.remotePrefix);
        Job job = new Job(localFile, remoteFilename, BitmagUtils.getChecksum(checksum), 
                getUrl(remoteFilename));
        
        return job;
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
