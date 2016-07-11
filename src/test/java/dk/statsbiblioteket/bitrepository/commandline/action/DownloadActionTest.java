package dk.statsbiblioteket.bitrepository.commandline.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
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
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
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
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public class DownloadActionTest {
    
    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
    
    @BeforeClass
    public void initializeBitrepositorySettings() {
        BitmagUtils.initialize(Paths.get("src/test/resources/testSettings/"), Paths.get("non-existing"));
    }
    
    @Test
    public void testDownloadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/DownloadActionTest-testDownloadFiles";
        String fileID1 = "target/DownloadActionTest-testDownloadFiles-file1";
        String fileID2 = "target/DownloadActionTest-testDownloadFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Paths.get(fileID1).toFile().deleteOnExit();
        Paths.get(fileID2).toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        GetFileClient client = mock(GetFileClient.class);
        FileExchange fileExchange = mock(FileExchange.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        ActionRunner runner = new ActionRunner(new DownloadAction(cmd, client, fileExchange, reporter));
        Thread t = new Thread(runner);
        t.start();
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID1), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID2), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verify(reporter, times(1)).reportStart(eq(fileID1));
        verify(reporter, times(1)).reportFinish(eq(fileID1));
        verify(reporter, times(1)).reportStart(eq(fileID2));
        verify(reporter, times(1)).reportFinish(eq(fileID2));
        verify(reporter, times(1)).printStatistics();
        verifyNoMoreInteractions(reporter);
        
        verifyNoMoreInteractions(client);
    }

    @Test
    public void testSkipDownloadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/DownloadActionTest-testSkipDownloadFiles";
        String fileID1 = "target/DownloadActionTest-testSkipDownloadFiles-file1";
        String fileID2 = "target/DownloadActionTest-testSkipDownloadFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Path file1Path = Paths.get(fileID1);
        Files.createFile(file1Path);
        file1Path.toFile().deleteOnExit();
        Paths.get(fileID2).toFile().deleteOnExit();
        
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        GetFileClient client = mock(GetFileClient.class);
        FileExchange fileExchange = mock(FileExchange.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        ActionRunner runner = new ActionRunner(new DownloadAction(cmd, client, fileExchange, reporter));
        Thread t = new Thread(runner);
        t.start();
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        
        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID2), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verify(reporter, times(1)).reportSkipFile(eq(fileID1));
        verify(reporter, times(1)).reportStart(eq(fileID2));
        verify(reporter, times(1)).reportFinish(eq(fileID2));
        verify(reporter, times(1)).printStatistics();
        verifyNoMoreInteractions(reporter);
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testRetryDownloadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/DownloadActionTest-testRetryDownloadFiles";
        String fileID1 = "target/DownloadActionTest-testRetryDownloadFiles-file1";
        String fileID2 = "target/DownloadActionTest-testRetryDownloadFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Paths.get(fileID1).toFile().deleteOnExit();
        Paths.get(fileID2).toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(eq(CliOptions.RETRY_OPT), anyString())).thenReturn("2");
        when(cmd.getOptionValue(eq(CliOptions.ASYNC_OPT), anyString())).thenCallRealMethod();
        
        GetFileClient client = mock(GetFileClient.class);
        FileExchange fileExchange = mock(FileExchange.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        ActionRunner runner = new ActionRunner(new DownloadAction(cmd, client, fileExchange, reporter));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID1), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        OperationFailedEvent firstFileFail = new OperationFailedEvent(TEST_COLLECTION, null, null);
        firstFileFail.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileFail);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID2), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        assertFalse(runner.getFinished());
        
        verify(client, timeout(3000).times(2)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID1), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        firstFileComplete.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verify(reporter, times(1)).reportStart(eq(fileID1));
        verify(reporter, times(1)).reportStart(eq(fileID2));
        verify(reporter, times(1)).reportFinish(eq(fileID2));
        verify(reporter, times(1)).reportFinish(eq(fileID1));
        verify(reporter, times(1)).printStatistics();
        verifyNoMoreInteractions(reporter);
        
        verifyNoMoreInteractions(client);
    }
    
    @Test
    public void testNoRetryDownloadFiles() throws InvalidParameterException, IOException, InterruptedException, SkipFileException {
        String outputSumFile = "target/DownloadActionTest-testNoRetryDownloadFiles";
        String fileID1 = "target/DownloadActionTest-testNoRetryDownloadFiles-file1";
        String fileID2 = "target/DownloadActionTest-testNoRetryDownloadFiles-file2";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        Paths.get(fileID1).toFile().deleteOnExit();
        Paths.get(fileID2).toFile().deleteOnExit();
        
        String remoteFileID1 = FileIDTranslationUtil.localToRemote(fileID1, null, null);
        String remoteFileID2 = FileIDTranslationUtil.localToRemote(fileID2, null, null);
        
        writeSumFile(outputSumFile, fileID1, fileID2);
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        when(cmd.getOptionValue(CliOptions.COLLECTION_OPT)).thenReturn(TEST_COLLECTION);
        when(cmd.getOptionValue(CliOptions.PILLAR_OPT)).thenReturn(TEST_PILLAR);
        when(cmd.getOptionValue(anyString(), anyString())).thenCallRealMethod();
        
        GetFileClient client = mock(GetFileClient.class);
        FileExchange fileExchange = mock(FileExchange.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        ActionRunner runner = new ActionRunner(new DownloadAction(cmd, client, fileExchange, reporter));
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID1), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        OperationFailedEvent firstFileFail = new OperationFailedEvent(TEST_COLLECTION, null, null);
        firstFileFail.setFileID(remoteFileID1);
        eventHandlerCaptor.getValue().handleEvent(firstFileFail);
        
        assertFalse(runner.getFinished());

        verify(client, timeout(3000).times(1)).getFileFromFastestPillar(eq(TEST_COLLECTION), eq(remoteFileID2), 
                (FilePart) isNull(), any(URL.class), eventHandlerCaptor.capture(), (String) isNull());
        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION, null);
        secondFileComplete.setFileID(remoteFileID2);
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        
        runner.waitForFinish(3000);
        assertTrue(runner.getFinished());
        
        verify(reporter, times(1)).reportStart(eq(fileID1));
        verify(reporter, times(1)).reportFailure(eq(fileID1));
        verify(reporter, times(1)).reportStart(eq(fileID2));
        verify(reporter, times(1)).reportFinish(eq(fileID2));
        verify(reporter, times(1)).printStatistics();
        verifyNoMoreInteractions(reporter);
        
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
