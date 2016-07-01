package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
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

import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;
import dk.statsbiblioteket.bitrepository.commandline.action.download.DownloadFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.action.download.DownloadJob;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;

public class DownloadAction extends RetryingConcurrentClientAction<DownloadJob> implements ClientAction {

    private GetFileClient getFileClient;
    private EventHandler eventHandler;

    public DownloadAction(CommandLine cmd, GetFileClient getFileClient, FileExchange fileExchange) {
        super(cmd);
        this.getFileClient = getFileClient;
        runningJobs = new RunningJobs<>(super.asyncJobs);
        eventHandler = new DownloadFilesEventHandler(fileExchange, runningJobs, failedJobsQueue, reporter);
        clientAction = Action.DOWNLOAD;
    }
    
    @Override
    protected void startJob(DownloadJob job) throws IOException {
        runningJobs.addJob(job);
        job.incrementAttempts();
        getFileClient.getFileFromFastestPillar(collectionID, job.getRemoteFileID(), null, job.getUrl(), eventHandler, null);
    }

    @Override
    protected DownloadJob createJob(String originalFilename, String checksum) throws SkipFileException, MalformedURLException {
        Path localFile = Paths.get(originalFilename);
        // Figure out how to handle file with prefixes when downloading..
        if(Files.exists(localFile)) {
            throw new SkipFileException("Skipping file as it already exists");
        }
        String remoteFilename = FileIDTranslationUtil.localToRemote(originalFilename, localPrefix, remotePrefix);
        DownloadJob job = new DownloadJob(localFile, remoteFilename, BitmagUtils.getChecksum(checksum), 
                getUrl(remoteFilename));
        
        return job;
    }
    
    private URL getUrl(String filename) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        String path = DigestUtils.md5Hex(collectionID + filename);
        return new URL(baseurl.toString() + path);
    }

}
