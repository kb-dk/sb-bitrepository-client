package dk.statsbiblioteket.bitrepository.commandline.action.list;

import java.util.List;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListFilesEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean partialResults = false;
    private final Object finishLock = new Object();
    private boolean finished = false;
    private List<ChecksumDataForChecksumSpecTYPE> checksumData;
    
    @Override
    public void handleEvent(OperationEvent event) {
    switch(event.getEventType()) {
    case COMPONENT_COMPLETE:
        log.debug("Got COMPONENT_COMPLETE event");
        if(event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent checksumsEvent = (ChecksumsCompletePillarEvent) event;
            checksumData = checksumsEvent.getChecksums().getChecksumDataItems();
            partialResults = checksumsEvent.isPartialResult();
        }
        case COMPLETE:
            log.info("Finished get file for file '{}'", event.getFileID());
            finish();
            break;
        case FAILED:
            log.warn("Failed get file for file '{}'", event.getFileID());
            finish();
            break;
        default:
            break;
        }    
    }
    
    public List<ChecksumDataForChecksumSpecTYPE> getChecksumData() {
        return checksumData;
    }
    
    private void finish() {
        log.trace("Finish method invoked");
        synchronized (finishLock) {
            finished = true;
            log.trace("Finish method entered synchronized block");
            finishLock.notifyAll();
            log.trace("Finish method notified All");            
        }
    }

    public void waitForFinish() throws InterruptedException {
        synchronized (finishLock) {
            log.trace("Thread waiting for put client to finish");
            if(finished == false) {
                finishLock.wait();
            }
            log.trace("Put client have indicated it's finished.");
        }
    }
    
    public boolean partialResults() {
        return partialResults;
    }
 
}
