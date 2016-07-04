package dk.statsbiblioteket.bitrepository.commandline.util;

import java.io.PrintStream;

import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;

/**
 * Class to handle reporting of status for files in a job 
 */
public class StatusReporter {

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
    
    public StatusReporter(PrintStream writer, Action operation) {
        this.writer = writer;
        this.operation = operation;
        
    }
    
    public void reportStart(String file) {
        started++;
        writer.format("%s %s of %s%n", START, operation, file);
        writer.flush();
    }
    
    public void reportFinish(String file) {
        finished++;
        writer.format("%s %s of %s%n", FINISH, operation, file);
        writer.flush();
    }
    
    public void reportFailure(String file) {
        failed++;
        writer.format("%s %s of %s%n", FAIL, operation, file);
        writer.flush();
    }
    
    public void reportSkipFile(String file) {
        skipped++;
        writer.format("%s %s of %s%n", SKIP, operation, file);
        writer.flush();
    }
    
    public void printStatistics() {
        writer.format("Started: %d, Finished: %d, Failed: %d, Skipped: %d%n", started, finished, failed, skipped);
        writer.flush();
    }
}
