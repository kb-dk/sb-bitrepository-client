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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.testutil.ActionRunner;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;


public class DeleteActionTest {
    
    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
    
    @BeforeClass
    public void initializeBitrepositorySettings() {
        BitmagUtils.initialize(Paths.get("src/test/resources/testSettings/"), Paths.get("non-existing"));
    }

    @Test
    public void testDeleteFiles() throws InvalidParameterException, IOException, InterruptedException {
        String outputSumFile = "target/DeleteActionTest-testDeleteFiles";
        String fileID1 = "file1";
        String fileID2 = "file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        DeleteFileClient client = mock(DeleteFileClient.class);
        
        ActionRunner runner = new ActionRunner(new DeleteAction(cmd, client));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).deleteFile(eq(TEST_COLLECTION), eq(fileID1), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(fileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(1)).deleteFile(eq(TEST_COLLECTION), eq(fileID2), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(fileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testRetryDeleteFiles() throws InvalidParameterException, IOException, InterruptedException {
        String outputSumFile = "target/DeleteActionTest-testRetryDeleteFiles";
        String fileID1 = "file1";
        String fileID2 = "file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(eq(CliOptions.RETRY_OPT), anyString())).thenReturn("2");
        when(cmd.getOptionValue(eq(CliOptions.ASYNC_OPT), anyString())).thenCallRealMethod();
        
        DeleteFileClient client = mock(DeleteFileClient.class);
        
        ActionRunner runner = new ActionRunner(new DeleteAction(cmd, client));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).deleteFile(eq(TEST_COLLECTION), eq(fileID1), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        OperationFailedEvent firstFileFail = new OperationFailedEvent(TEST_COLLECTION, null, null);
        firstFileFail.setFileID(fileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileFail);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(1)).deleteFile(eq(TEST_COLLECTION), eq(fileID2), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(fileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(2)).deleteFile(eq(TEST_COLLECTION), eq(fileID1), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(fileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testNoRetryDeleteFiles() throws InvalidParameterException, IOException, InterruptedException {
        String outputSumFile = "target/DeleteActionTest-testNoRetryDeleteFiles";
        String fileID1 = "file1";
        String fileID2 = "file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        DeleteFileClient client = mock(DeleteFileClient.class);
        
        ActionRunner runner = new ActionRunner(new DeleteAction(cmd, client));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).deleteFile(eq(TEST_COLLECTION), eq(fileID1), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        OperationFailedEvent firstFileFail = new OperationFailedEvent(TEST_COLLECTION, null, null);
        firstFileFail.setFileID(fileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileFail);
        
        assertFalse(runner.getFinished());

        verify(client, timeout(3000).times(1)).deleteFile(eq(TEST_COLLECTION), eq(fileID2), eq(TEST_PILLAR), 
                any(ChecksumDataForFileTYPE.class), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), 
                (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(fileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
    
    private void writeSumFile(String sumfile, String... fileIDs) throws IOException {
        try (MD5SumFileWriter writer = new MD5SumFileWriter(Paths.get(sumfile))) {
            for(String fileID : fileIDs) {
                writer.writeChecksumLine(Paths.get(fileID), "abab");
            }
        }
    }

}
