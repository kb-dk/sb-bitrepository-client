package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.action.list.ListFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.util.ArgumentValidationUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;

public class ListAction implements ClientAction {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final static int PAGE_SIZE = 10000;
    private GetChecksumsClient getChecksumsClient;
    
    private final String collectionID;
    private final String pillarID;
    private String localPrefix = null;
    private String remotePrefix = null;
    private Path sumFile;
    private MD5SumFileWriter md5SumFileWriter;
    
    public ListAction(CommandLine cmd, GetChecksumsClient getChecksumsClient) throws InvalidParameterException {
        this.getChecksumsClient = getChecksumsClient;
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        pillarID = cmd.getOptionValue(CliOptions.PILLAR_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        localPrefix = cmd.hasOption(CliOptions.LOCAL_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT) : null;
        remotePrefix = cmd.hasOption(CliOptions.REMOTE_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT) : null;
        try {
            md5SumFileWriter = new MD5SumFileWriter(sumFile);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        ArgumentValidationUtils.validateCollection(collectionID);
        ArgumentValidationUtils.validatePillar(pillarID, collectionID);
    }
    
    public void performAction() {
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        Date latestResultDate = new Date(0);
        boolean notFinished = true;
        try {
            do {
                ListFilesEventHandler eventHandler = new ListFilesEventHandler();
                ContributorQuery[] query = makeQuery(latestResultDate);
                getChecksumsClient.getChecksums(collectionID, query, null, checksumSpec, null, eventHandler, null);
                eventHandler.waitForFinish();
                latestResultDate = reportResults(eventHandler.getChecksumData());
                notFinished = eventHandler.partialResults();
            } while(notFinished);
        } catch (InterruptedException e) {
            log.error("Got interrupted while getting checksums", e);
            throw new RuntimeException(e);
        }
        try {
            md5SumFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Date reportResults(List<ChecksumDataForChecksumSpecTYPE> results) {
        Date latestDate = new Date(0);
        for (ChecksumDataForChecksumSpecTYPE checksumData : results) {
            Date calculationDate = CalendarUtils.convertFromXMLGregorianCalendar(checksumData.getCalculationTimestamp());
            if(calculationDate.after(latestDate)) {
                latestDate = calculationDate;
            }
            Path file;
            try {
                file = Paths.get(FileIDTranslationUtil.remoteToLocal(checksumData.getFileID(), localPrefix, remotePrefix));
            } catch (SkipFileException e) {
                log.debug("Skipping file '{}' due to '{}'", checksumData.getFileID(), e.getMessage());
                continue;
            }
            String checksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            try {
                md5SumFileWriter.writeChecksumLine(file, checksum);
            } catch (IOException e) {
                log.error("Failed to report checksum for file {}", file.toString(), e);
            }
        }    
        return latestDate;
    }
    
    private ContributorQuery[] makeQuery(Date latestResult) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        res.add(new ContributorQuery(pillarID, latestResult, null, PAGE_SIZE));
        return res.toArray(new ContributorQuery[1]);
    }

}
