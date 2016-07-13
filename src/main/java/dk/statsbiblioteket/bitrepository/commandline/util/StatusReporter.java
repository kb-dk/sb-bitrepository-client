package dk.statsbiblioteket.bitrepository.commandline.util;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;

/**
 * Class to handle reporting of status for files in a job 
 */
public class StatusReporter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final static String START = "[STARTING]:";
    private final static String FINISH = "[FINISHED]:";
    private final static String FAIL = "[FAILED]:";
    private final static String SKIP = "[SKIPPING]:";
    
    private int started = 0;
    private int finished = 0;
    private int failed = 0;
    private int skipped = 0;
    
    private PrintStream writer;
    private Action operation;
    
    /**
     *  Constructor
     *  @param writer The {@link PrintStream} to output the results to
     *  @param operation The type of {@link Action} to report statuses for 
     */
    public StatusReporter(PrintStream writer, Action operation) {
        this.writer = writer;
        this.operation = operation;
        
    }
    
    /**
     * Report that processing of a file has started 
     * @param file The ID of the file
     */
    public void reportStart(String file) {
        log.debug("Starting {} operation for '{}'", operation, file);
        started++;
        writer.format("%s %s of %s%n", START, operation, file);
        writer.flush();
    }

    /**
     * Report that processing of a file has finished successfully  
     * @param file The ID of the file
     */
    public void reportFinish(String file) {
        log.debug("Finished {} operation for '{}'", operation, file);
        finished++;
        writer.format("%s %s of %s%n", FINISH, operation, file);
        writer.flush();
    }
    
    /**
     * Report that processing of a file has failed 
     * @param file The ID of the file
     */
    public void reportFailure(String file) {
        log.debug("Failed {} operation for '{}'", operation, file);
        failed++;
        writer.format("%s %s of %s%n", FAIL, operation, file);
        writer.flush();
    }
    
    /**
     * Report that processing of a file has been skipped 
     * @param file The ID of the file
     */
    public void reportSkipFile(String file) {
        log.debug("Skipped {} operation for '{}'", operation, file);
        skipped++;
        writer.format("%s %s of %s%n", SKIP, operation, file);
        writer.flush();
    }
    
    /**
     * Print statistics of files that has been reported. 
     */
    public void printStatistics() {
        writer.format("Started: %d, Finished: %d, Failed: %d, Skipped: %d%n", started, finished, failed, skipped);
        writer.flush();
    }
}
