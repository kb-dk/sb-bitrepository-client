package dk.statsbiblioteket.bitrepository.commandline.action.upload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.FileExchange;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

public class PutFilesEventHandlerTest {

    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
        
    @Test
    public void testIdentificationCompleteEvent() throws IOException, URISyntaxException {
        String testFileID = "target/PutFilesEventHandlerTest-testIdentificationCompleteEvent";
        URL fileExchangeUrl = new URL("http://fake-server/dav/file1");   
        Path testFile = Paths.get(testFileID);
        Files.createFile(testFile);
        testFile.toFile().deleteOnExit();
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        FileExchange fileExchange = mock(FileExchange.class);
        
        Job testJob = new Job(testFile, testFileID, null, fileExchangeUrl);
        when(runningJobs.getJob(testFileID)).thenReturn(testJob);
        
        PutFilesEventHandler handler = new PutFilesEventHandler(fileExchange, runningJobs, failedQueue, 
                reporter);
        
        IdentificationCompleteEvent idCompleteEvent = new IdentificationCompleteEvent(TEST_COLLECTION, 
                Arrays.asList(TEST_PILLAR));
        idCompleteEvent.setFileID(testFileID);
        handler.handleEvent(idCompleteEvent);
        
        verify(runningJobs, times(1)).getJob(eq(testFileID));
        verify(fileExchange, times(1)).putFile(any(InputStream.class), eq(testJob.getUrl()));
        
        verifyNoMoreInteractions(runningJobs);
        verifyNoMoreInteractions(reporter);
        verifyNoMoreInteractions(fileExchange);
        verifyNoMoreInteractions(failedQueue);
    }
    
    @Test
    public void testCompleteEvent() throws IOException, URISyntaxException {
        String testFileID = "target/PutFilesEventHandlerTest-testCompleteEvent";
        URL fileExchangeUrl = new URL("http://fake-server/dav/file1");    
        Paths.get(testFileID).toFile().deleteOnExit();
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        FileExchange fileExchange = mock(FileExchange.class);
        
        Job testJob = new Job(Paths.get(testFileID), testFileID, null, fileExchangeUrl);
        when(runningJobs.getJob(testFileID)).thenReturn(testJob);
        
        PutFilesEventHandler handler = new PutFilesEventHandler(fileExchange, runningJobs, failedQueue, 
                reporter);
        
        CompleteEvent completeEvent = new CompleteEvent(TEST_COLLECTION, null);
        completeEvent.setFileID(testFileID);
        handler.handleEvent(completeEvent);
        
        verify(runningJobs, times(1)).getJob(eq(testFileID));
        verify(fileExchange, times(1)).deleteFile(eq(testJob.getUrl()));
        
        verify(runningJobs, times(1)).removeJob(eq(testJob));
        verify(reporter, times(1)).reportFinish(eq(testFileID));
        
        verifyNoMoreInteractions(runningJobs);
        verifyNoMoreInteractions(reporter);
        verifyNoMoreInteractions(fileExchange);
        verifyNoMoreInteractions(failedQueue);
    }
    
    @Test
    public void testFailureEvent() throws IOException, URISyntaxException {
        String testFileID = "target/PutFilesEventHandlerTest-testFailureEvent";
        URL fileExchangeUrl = new URL("http://fake-server/dav/file1");    
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        FileExchange fileExchange = mock(FileExchange.class);
        
        Job testJob = new Job(Paths.get(testFileID), testFileID, null, fileExchangeUrl);
        when(runningJobs.getJob(testFileID)).thenReturn(testJob);
        
        PutFilesEventHandler handler = new PutFilesEventHandler(fileExchange, runningJobs, failedQueue, 
                reporter);
        
        OperationFailedEvent completeEvent = new OperationFailedEvent(TEST_COLLECTION, null ,null);
        completeEvent.setFileID(testFileID);
        handler.handleEvent(completeEvent);
        
        verify(runningJobs, times(1)).getJob(eq(testFileID));
        verify(fileExchange, times(1)).deleteFile(eq(testJob.getUrl()));
        
        verify(runningJobs, times(1)).removeJob(eq(testJob));
        verify(failedQueue, times(1)).add(eq(testJob));
        
        verifyNoMoreInteractions(runningJobs);
        verifyNoMoreInteractions(reporter);
        verifyNoMoreInteractions(fileExchange);
        verifyNoMoreInteractions(failedQueue);
    }
    
    @Test
    public void testFileExchangeFailure() throws IOException, URISyntaxException {
        String testFileID = "target/PutFilesEventHandlerTest-testFileExchangeFailure";
        URL fileExchangeUrl = new URL("http://fake-server/dav/file1");
        Path testFile = Paths.get(testFileID);
        Files.createFile(testFile);
        testFile.toFile().deleteOnExit();
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        FileExchange fileExchange = mock(FileExchange.class);
        
        doAnswer(new Answer<Object>() {
            @Override 
            public Object answer(InvocationOnMock invocationOnMock) throws IOException {
                Object[] args = invocationOnMock.getArguments();
                throw new IOException("Bogus IOException testing fileexchange failure");
            }
        }).when(fileExchange).putFile(any(InputStream.class), any(URL.class));
        
        Job testJob = new Job(testFile, testFileID, null, fileExchangeUrl);
        when(runningJobs.getJob(testFileID)).thenReturn(testJob);
        
        PutFilesEventHandler handler = new PutFilesEventHandler(fileExchange, runningJobs, failedQueue, 
                reporter);
        
        IdentificationCompleteEvent completeEvent = new IdentificationCompleteEvent(TEST_COLLECTION, 
                Arrays.asList(TEST_PILLAR));
        completeEvent.setFileID(testFileID);
        handler.handleEvent(completeEvent);
        
        verify(runningJobs, times(1)).getJob(eq(testFileID));
        verify(fileExchange, times(1)).putFile(any(InputStream.class), eq(testJob.getUrl()));
        
        verify(runningJobs, times(1)).removeJob(eq(testJob));
        verify(failedQueue, times(1)).add(eq(testJob));
        verify(fileExchange, times(1)).deleteFile(eq(testJob.getUrl()));
        
        verifyNoMoreInteractions(runningJobs);
        verifyNoMoreInteractions(reporter);
        verifyNoMoreInteractions(fileExchange);
        verifyNoMoreInteractions(failedQueue);
    }
    
}
