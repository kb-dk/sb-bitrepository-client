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
    
    private PrintStream writer;

    public StatusReporter(PrintStream writer) {
        this.writer = writer;
        
    }
    
    public void reportStart(Action operation, String file) {
        writer.format("%s %s of %s%n", START, operation, file);
        writer.flush();
    }
    
    public void reportFinish(Action operation, String file) {
        writer.format("%s %s of %s%n", FINISH, operation, file);
        writer.flush();
    }
    
    public void reportFailure(Action operation, String file) {
        writer.format("%s %s of %s%n", FAIL, operation, file);
        writer.flush();
    }
    
    public void reportSkipFile(Action operation, String file) {
        writer.format("%s %s of %s%n", SKIP, operation, file);
        writer.flush();
    }
}
