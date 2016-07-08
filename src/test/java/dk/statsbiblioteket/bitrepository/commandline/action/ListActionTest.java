package dk.statsbiblioteket.bitrepository.commandline.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.testutil.ActionRunner;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;

public class ListActionTest {
    
    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
    
    @BeforeClass
    public void initializeBitrepositorySettings() {
        BitmagUtils.initialize(Paths.get("src/test/resources/testSettings/"), Paths.get("non-existing"));
    }
    
    @Test
    public void testListFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/ListActionTest-testListFiles";
        String fileID1 = "target/ListActionTest-testListFiles-file1";
        String fileID2 = "target/ListActionTest-testListFiles-file2";
        Paths.get(outputSumFile).toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        GetChecksumsClient client = mock(GetChecksumsClient.class);
        
        ActionRunner runner = new ActionRunner(new ListAction(cmd, client));
        Thread t = new Thread(runner);
        t.start();
        
        ContributorQuery[] firstQuery = makeQuery(TEST_PILLAR, new Date(0));
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getChecksums(eq(TEST_COLLECTION), eq(firstQuery), (String) isNull(),
                any(ChecksumSpecTYPE.class), (URL) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        
        Date checksumTime = new Date();
        ChecksumsCompletePillarEvent completePillarEvent = createPillarCompleteEvent(checksumTime, remoteFileID1, 
                true);
        eventHandlerCaptor.getValue().handleEvent(completePillarEvent);
        
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        assertFalse(runner.getFinished());
        ContributorQuery[] secondQuery = makeQuery(TEST_PILLAR, checksumTime);
        verify(client, timeout(3000).times(1)).getChecksums(eq(TEST_COLLECTION), eq(secondQuery), (String) isNull(),
                any(ChecksumSpecTYPE.class), (URL) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        
        checksumTime = new Date();
        ChecksumsCompletePillarEvent secondCompletePillarEvent = createPillarCompleteEvent(checksumTime, remoteFileID2, 
                false);
        eventHandlerCaptor.getValue().handleEvent(secondCompletePillarEvent);
        
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testFailListFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/ListActionTest-testFailListFiles";
        Paths.get(outputSumFile).toFile().deleteOnExit();
         
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        GetChecksumsClient client = mock(GetChecksumsClient.class);
        
        ActionRunner runner = new ActionRunner(new ListAction(cmd, client));
        Thread t = new Thread(runner);
        t.start();
        
        ContributorQuery[] firstQuery = makeQuery(TEST_PILLAR, new Date(0));
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getChecksums(eq(TEST_COLLECTION), eq(firstQuery), (String) isNull(),
                any(ChecksumSpecTYPE.class), (URL) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        
        OperationFailedEvent failureEvent = new OperationFailedEvent(TEST_COLLECTION, null, null);
        eventHandlerCaptor.getValue().handleEvent(failureEvent);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
        
    private ChecksumsCompletePillarEvent createPillarCompleteEvent(Date checksumTime, String fileID, 
            boolean partialResults) {
        ResultingChecksums rc = new ResultingChecksums();
        List<ChecksumDataForChecksumSpecTYPE> items = rc.getChecksumDataItems();
        ChecksumDataForChecksumSpecTYPE item1 = new ChecksumDataForChecksumSpecTYPE();
        item1.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(checksumTime));
        item1.setChecksumValue(Base16Utils.encodeBase16("abab"));
        item1.setFileID(fileID);
        items.add(item1);
        
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        
        ChecksumsCompletePillarEvent pillarEvent = new ChecksumsCompletePillarEvent(TEST_PILLAR, 
                TEST_COLLECTION, rc, checksumSpec, partialResults);
        return pillarEvent;
    }
    
    private ContributorQuery[] makeQuery(String pillarID, Date latestResult) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        res.add(new ContributorQuery(pillarID, latestResult, null, ListAction.PAGE_SIZE));
        return res.toArray(new ContributorQuery[1]);
    }
}
