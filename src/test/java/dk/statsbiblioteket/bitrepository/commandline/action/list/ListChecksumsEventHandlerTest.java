package dk.statsbiblioteket.bitrepository.commandline.action.list;

import java.util.List;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ListChecksumsEventHandlerTest {

    public final static String TEST_COLLECTION = "collection1";
    public final static String TEST_PILLAR = "test-pillar";
    
    @Test
    public void testSuccess() throws InterruptedException {
        ListChecksumsEventHandler eventHandler = new ListChecksumsEventHandler(TEST_PILLAR);
        
        FinishWaiter waiter = new FinishWaiter(eventHandler);
        Thread t = new Thread(waiter);
        t.start();
        
        Assert.assertFalse(waiter.getFinished());
        
        ResultingChecksums rc = new ResultingChecksums();
        List<ChecksumDataForChecksumSpecTYPE> items = rc.getChecksumDataItems();
        ChecksumDataForChecksumSpecTYPE item1 = new ChecksumDataForChecksumSpecTYPE();
        item1.setCalculationTimestamp(CalendarUtils.getNow());
        item1.setChecksumValue(Base16Utils.encodeBase16("abab"));
        item1.setFileID("testFile1");
        items.add(item1);
        
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        
        ChecksumsCompletePillarEvent pillarEvent = new ChecksumsCompletePillarEvent(TEST_PILLAR, 
                TEST_COLLECTION, rc, checksumSpec, false);
        
        CompleteEvent completeEvent = new CompleteEvent(TEST_COLLECTION, null);
        eventHandler.handleEvent(pillarEvent);
        
        Assert.assertFalse(waiter.finished);
        
        eventHandler.handleEvent(completeEvent);
        
        waiter.waitForFinish(1000);
        Assert.assertTrue(waiter.getFinished());
        Assert.assertFalse(eventHandler.partialResults());
        Assert.assertFalse(eventHandler.hasFailed());
        Assert.assertEquals(eventHandler.getChecksumData(), rc.getChecksumDataItems());
        
    }
    
    @Test
    public void testFailure() throws InterruptedException {
        ListChecksumsEventHandler eventHandler = new ListChecksumsEventHandler(TEST_PILLAR);
        
        FinishWaiter waiter = new FinishWaiter(eventHandler);
        Thread t = new Thread(waiter);
        t.start();
        
        Assert.assertFalse(waiter.getFinished());
        
        OperationFailedEvent failedEvent = new OperationFailedEvent(TEST_COLLECTION, null, null);
        eventHandler.handleEvent(failedEvent);
        
        waiter.waitForFinish(1000);
        Assert.assertTrue(waiter.getFinished());
        Assert.assertTrue(eventHandler.hasFailed());
        Assert.assertFalse(eventHandler.partialResults());
        Assert.assertNull(eventHandler.getChecksumData());
    }
    
    private class FinishWaiter implements Runnable {
        private final Object finishLock = new Object();
        boolean finished = false;
        ListChecksumsEventHandler handler;
        
        public FinishWaiter(ListChecksumsEventHandler handler) {
            this.handler = handler;
        }
        
        public boolean getFinished() {
            return finished;
        }
        
        private void finish() {
            synchronized (finishLock) {
                finished = true;
                finishLock.notifyAll();
            }
        }
        
        public void waitForFinish(long timeout) throws InterruptedException {
            synchronized (finishLock) {
                if(finished == false) {
                    finishLock.wait(timeout);
                }
            }
        }
        
        public void run() {
            try {
                handler.waitForFinish();
            } catch (Exception e) {
                // Err, not sure if we want to do anything?
            }
            finish();
        }
    }
}
