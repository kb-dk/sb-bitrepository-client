package dk.statsbiblioteket.bitrepository.commandline.action.list;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;

public class ListFilesEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Date latestDate = new Date(0);
    private boolean completeResults = false;

    private MD5SumFileWriter resultWriter;
    
    public ListFilesEventHandler(MD5SumFileWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    @Override
    public void handleEvent(OperationEvent event) {
    switch(event.getEventType()) {
    case COMPONENT_COMPLETE:
        if(event.getEventType().equals(ChecksumsCompletePillarEvent.class)) {
            ChecksumsCompletePillarEvent checksumsEvent = (ChecksumsCompletePillarEvent) event;
            writeResults(checksumsEvent.getChecksums());
            if(checksumsEvent.isPartialResult()) {
                completeResults = false;
            } else {
                completeResults = true;
            }
        }
        case COMPLETE:
            log.info("Finished get file for file '{}'", event.getFileID());
            break;
        case FAILED:
            log.warn("Failed get file for file '{}'", event.getFileID());
            break;
        default:
            break;
        }    
    }
    
    public Date getLatestDate() {
        return latestDate;
    }
    
    public boolean completeResults() {
        return completeResults;
    }
    
    private void writeResults(ResultingChecksums resultingChecksums) {
        for (ChecksumDataForChecksumSpecTYPE checksumData : resultingChecksums.getChecksumDataItems()) {
            Date calculationDate = CalendarUtils.convertFromXMLGregorianCalendar(checksumData.getCalculationTimestamp());
            if(calculationDate.after(latestDate)) {
                latestDate = calculationDate;
            }
            Path file = Paths.get(checksumData.getFileID());
            String checksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            try {
                resultWriter.writeChecksumLine(file, checksum);
            } catch (IOException e) {
                log.error("Failed to report checksum for file {}", file.toString(), e);
            }
        }
    }
}
