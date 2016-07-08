package dk.statsbiblioteket.bitrepository.commandline.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.testutil.ActionRunner;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;

public class UploadActionTest {
    
    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
    
    @BeforeClass
    public void initializeBitrepositorySettings() {
        BitmagUtils.initialize(Paths.get("src/test/resources/testSettings/"), Paths.get("non-existing"));
    }
    
    @Test
    public void testUploadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/UploadActionTest-testUploadFiles";
        String fileID1 = "target/UploadActionTest-testUploadFiles-file1";
        String fileID2 = "target/UploadActionTest-testUploadFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Path file1Path = Paths.get(fileID1);
        Files.createFile(file1Path);
        file1Path.toFile().deleteOnExit();
        Path file2Path = Paths.get(fileID2);
        Files.createFile(file2Path);
        file2Path.toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        PutFileClient client = mock(PutFileClient.class);
        FileExchange fileExchanget = mock(FileExchange.class);
        
        ActionRunner runner = new ActionRunner(new UploadAction(cmd, client, fileExchanget));
        Thread t = new Thread(runner);
        t.start();
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID1), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(1)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID2), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testRetryUploadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/UploadActionTest-testRetryUploadFiles";
        String fileID1 = "target/UploadActionTest-testRetryUploadFiles-file1";
        String fileID2 = "target/UploadActionTest-testRetryUploadFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Path file1Path = Paths.get(fileID1);
        Files.createFile(file1Path);
        file1Path.toFile().deleteOnExit();
        Path file2Path = Paths.get(fileID2);
        Files.createFile(file2Path);
        file2Path.toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(eq(CliOptions.RETRY_OPT), anyString())).thenReturn("2");
        when(cmd.getOptionValue(eq(CliOptions.ASYNC_OPT), anyString())).thenCallRealMethod();
        
        PutFileClient client = mock(PutFileClient.class);
        FileExchange fileExchanget = mock(FileExchange.class);

        ActionRunner runner = new ActionRunner(new UploadAction(cmd, client, fileExchanget));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID1), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        OperationFailedEvent firstFileFail = new OperationFailedEvent(TEST_COLLECTION, null, null);
        firstFileFail.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileFail);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(1)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID2), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(2)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID1), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testNoRetryUploadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/UploadActionTest-testNoRetryRetryFiles";
        String fileID1 = "target/UploadActionTest-testNoRetryRetryFiles-file1";
        String fileID2 = "target/UploadActionTest-testNoRetryRetryFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Path file1Path = Paths.get(fileID1);
        Files.createFile(file1Path);
        file1Path.toFile().deleteOnExit();
        Path file2Path = Paths.get(fileID2);
        Files.createFile(file2Path);
        file2Path.toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        PutFileClient client = mock(PutFileClient.class);
        FileExchange fileExchanget = mock(FileExchange.class);

        ActionRunner runner = new ActionRunner(new UploadAction(cmd, client, fileExchanget));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID1), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        OperationFailedEvent firstFileFail = new OperationFailedEvent(TEST_COLLECTION, null, null);
        firstFileFail.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileFail);
        
        assertFalse(runner.getFinished());

        verify(client, timeout(3000).times(1)).putFile(eq(TEST_COLLECTION), any(URL.class), 
                eq(remoteFileID2), anyLong(), any(ChecksumDataForFileTYPE.class), 
                (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
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
