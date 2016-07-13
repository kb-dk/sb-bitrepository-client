package dk.statsbiblioteket.bitrepository.commandline.action.delete;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.action.job.Job;
import dk.statsbiblioteket.bitrepository.commandline.action.job.RunningJobs;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;


public class DeleteFilesEventHandlerTest {

    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
    public final static String TEST_FILEID = "testfile";
    
    @Test
    public void testCompleteEvent() {
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        Job testJob = new Job(Paths.get(TEST_FILEID), TEST_FILEID, null, null);
        when(runningJobs.getJob(TEST_FILEID)).thenReturn(testJob);
        
        DeleteFilesEventHandler handler = new DeleteFilesEventHandler(runningJobs, failedQueue, reporter);
        
        CompleteEvent completeEvent = new CompleteEvent(TEST_COLLECTION, null);
        completeEvent.setFileID(TEST_FILEID);
        handler.handleEvent(completeEvent);
        
        InOrder order = inOrder(runningJobs, reporter, failedQueue);        
        order.verify(runningJobs, times(1)).getJob(eq(TEST_FILEID));
        order.verify(reporter, times(1)).reportFinish(eq(TEST_FILEID));
        order.verify(runningJobs, times(1)).removeJob(eq(testJob));
        order.verifyNoMoreInteractions(); 
    }
    
    @Test
    public void testFailedEvent() {
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        Job testJob = new Job(Paths.get(TEST_FILEID), TEST_FILEID, null, null);
        when(runningJobs.getJob(TEST_FILEID)).thenReturn(testJob);
        
        DeleteFilesEventHandler handler = new DeleteFilesEventHandler(runningJobs, failedQueue, reporter);
        
        OperationFailedEvent failedEvent = new OperationFailedEvent(TEST_COLLECTION, null, null);
        failedEvent.setFileID(TEST_FILEID);
        handler.handleEvent(failedEvent);
        
        InOrder order = inOrder(runningJobs, reporter, failedQueue);
        order.verify(runningJobs, times(1)).getJob(eq(TEST_FILEID));
        order.verify(failedQueue, times(1)).add(eq(testJob));
        order.verify(runningJobs, times(1)).removeJob(eq(testJob));
        order.verifyNoMoreInteractions();
    }
    
    @Test
    public void testUnhandledEvent() {
        RunningJobs runningJobs = mock(RunningJobs.class);
        BlockingQueue<Job> failedQueue = (BlockingQueue<Job>) mock(BlockingQueue.class);
        StatusReporter reporter = mock(StatusReporter.class);
        
        Job testJob = new Job(Paths.get(TEST_FILEID), TEST_FILEID, null, null);
        when(runningJobs.getJob(TEST_FILEID)).thenReturn(testJob);
        
        DeleteFilesEventHandler handler = new DeleteFilesEventHandler(runningJobs, failedQueue, reporter);
        
        IdentificationCompleteEvent identificationEvent = new IdentificationCompleteEvent(TEST_COLLECTION, 
                Arrays.asList(TEST_PILLAR));
        identificationEvent.setFileID(TEST_FILEID);
        handler.handleEvent(identificationEvent);
        
        InOrder order = inOrder(runningJobs, reporter, failedQueue);
        
        order.verify(runningJobs, times(1)).getJob(eq(TEST_FILEID));
        order.verifyNoMoreInteractions();
    }
}
