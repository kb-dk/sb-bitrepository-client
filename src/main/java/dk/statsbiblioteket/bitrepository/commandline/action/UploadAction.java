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

import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;
import dk.statsbiblioteket.bitrepository.commandline.action.upload.PutFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.action.upload.PutJob;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public class UploadAction extends RetryingConcurrentClientAction<PutJob> implements ClientAction {

    private final Logger log = LoggerFactory.getLogger(getClass());
        
    private PutFileClient putFileClient;
    private EventHandler eventHandler;
    
    public UploadAction(CommandLine cmd, PutFileClient putFileClient, FileExchange fileExchange) throws InvalidParameterException {
        super(cmd, new StatusReporter(System.err, Action.UPLOAD));
        this.putFileClient = putFileClient;
        eventHandler = new PutFilesEventHandler(fileExchange, runningJobs, super.failedJobsQueue, super.reporter);
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
