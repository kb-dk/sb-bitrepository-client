package dk.statsbiblioteket.bitrepository.commandline;

import java.util.Arrays;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;

public class CliOptions {
    public final static String ACTION_OPT = "a";
    public final static String COLLECTION_OPT = "c";
    public final static String SOURCE_OPT = "s";
    public final static String SUMFILE_OPT = "f";
    public final static String PILLAR_OPT = "p";
    public final static String DESTINATION_OPT = "d";
    public final static String TYPE_OPT = "t";
    public final static String HELP_OPT = "h";
    public final static String LOCAL_PREFIX_OPT = "l";
    public final static String REMOTE_PREFIX_OPT = "r";
    public final static String RETRY_OPT = "x";
    public final static String ASYNC_OPT = "n";
    
    private final static Option actionOption;
    private final static Option actionDetailedOption;
    private final static Option collectionOption 
        = new Option(COLLECTION_OPT, "collection", true, "Collection to work on");
    private final static Option sourceOption 
        = new Option(SOURCE_OPT, "source", true, "Source directory to get files from");
    private final static Option sumfileOption 
        = new Option(SUMFILE_OPT, "sumfile", true, "Sumfile containing list of files to work on");
    private final static Option localOption 
        = new Option(LOCAL_PREFIX_OPT, "local-prefix", true, "Local prefix for operations");
    private final static Option remoteOption 
        = new Option(REMOTE_PREFIX_OPT, "remote-prefix", true, "Remote prefix for operations");
    private final static Option pillarOption 
        = new Option(PILLAR_OPT, "pillar", true, "Pillar to perform delete on");
    private final static Option destinationOption 
        = new Option(DESTINATION_OPT, "destination", true, "Destination directory to place files in");
    private final static Option typeOption 
        = new Option(TYPE_OPT, "type", true, "Type of listing");
    private final static Option retryOption 
        = new Option(RETRY_OPT, "retrys", true, "Number of retries before failing a file (Default: no retries)");
    private final static Option parallelOption 
        = new Option(ASYNC_OPT, "parallel", true, "Number of parallel operations (Default: 1)");
    private final static Option helpOption = new Option(HELP_OPT, "help", false, "Prints help and usage information");
    
    
    
    static {
        actionOption = new Option(ACTION_OPT, "action", true, "Action to perform");
        actionOption.setRequired(true);
        actionDetailedOption = new Option(ACTION_OPT, "action", true, "Possible actions: " + Arrays.asList(Action.values()));
        actionDetailedOption.setRequired(true);
    }
    
    /**
     * Method to print the help message
     * @param scriptName The name of the program as it was invoked
     * @param options The options to include in the help message 
     */
    public static void printHelp(String scriptName, Options options) {
        String header = "Batch client to work with bitrepository.org bitrepository\n\n";
        String footer = "\nPlease report issues at https://github.com/statsbiblioteket/sb-bitrepository-client";
        
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(scriptName, header, options, footer, true);
    }
    
    /**
     * Method to print the help information when no action is specified.  
     * @param scriptName The name of the program as it was invoked
     */
    public static void printActionHelp(String scriptName) {
        Options opts = new Options();
        opts.addOption(actionDetailedOption);
        printHelp(scriptName, opts);
        
    }
    
    /**
     * Method to retrieve all the various options available for usage
     * @return The options for use in the client.  
     */
    public static Options getAllOptions() {
        Options options = new Options();
        options.addOption(actionOption);
        options.addOption(collectionOption);
        options.addOption(sourceOption);
        options.addOption(sumfileOption);
        options.addOption(localOption);
        options.addOption(remoteOption);
        options.addOption(pillarOption);
        options.addOption(destinationOption);
        options.addOption(typeOption);
        options.addOption(retryOption);
        options.addOption(parallelOption);
        options.addOption(helpOption);
        return options;
    }
    
    /**
     * Method to retrieve the options that are relevant for a specific Action
     * @param action The {@link Action} to obtain options for
     * @return The options relevant for the given Action 
     */
    public static Options getActionOptions(Action action) {
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(actionOption);
        
        switch(action) {
        case MAKECHECKUSMS:
            options.addOption(makeOptionRequired(sourceOption));
            options.addOption(makeOptionRequired(sumfileOption));
            break;
        case UPLOAD: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption(localOption);
            options.addOption(remoteOption);
            options.addOption(retryOption);
            options.addOption(parallelOption);
            break;
        case LIST: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption(makeOptionRequired(pillarOption));
            options.addOption(localOption);
            options.addOption(remoteOption);
            break;
        case DOWNLOAD: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption(localOption);
            options.addOption(remoteOption);
            options.addOption(retryOption);
            options.addOption(parallelOption);
            break; 
        case DELETE:
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(pillarOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption(localOption);
            options.addOption(remoteOption);
            options.addOption(retryOption);
            options.addOption(parallelOption);
            break;
        }
        
        return options;
    }
    
    /**
     * Helper method to clone and make an option required for the specific use.
     * @param opt The option to deliver a clone marked as required
     * @return clone of the inputtet option marked as required  
     */
    private static Option makeOptionRequired(Option opt) {
        opt.setRequired(true);
        return opt;
    }
}
