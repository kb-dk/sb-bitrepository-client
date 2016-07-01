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

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.action.list.ListFilesEventHandler;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;

public class ListAction implements ClientAction {

    private final static int PAGE_SIZE = 10000;
    private GetChecksumsClient getChecksumsClient;

    private String collectionID;
    private String pillarID;
    private String localPrefix = null;
    private String remotePrefix = null;
    private Path sumFile;
    
    private ListFilesEventHandler eventHandler;
    private MD5SumFileWriter md5SumFileWriter;
    
    public ListAction(CommandLine cmd, GetChecksumsClient getChecksumsClient) {
        this.getChecksumsClient = getChecksumsClient;
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        localPrefix = cmd.hasOption(CliOptions.LOCAL_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT) : null;
        remotePrefix = cmd.hasOption(CliOptions.REMOTE_PREFIX_OPT) ? cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT) : null;
        try {
            md5SumFileWriter = new MD5SumFileWriter(sumFile);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        eventHandler = new ListFilesEventHandler(md5SumFileWriter);
    }
    
    public void performAction() {
        
    }

    
    private ContributorQuery[] makeQuery() {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        Date latestResult = eventHandler.getLatestDate();
        res.add(new ContributorQuery(pillarID, latestResult, null, PAGE_SIZE));
        return res.toArray(new ContributorQuery[1]);
    }
}
