package dk.statsbiblioteket.bitrepository.commandline;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import dk.statsbiblioteket.bitrepository.commandline.Commandline.Action;

public class CliOptions {
    private final static Option actionOption;
    private final static Option collectionOption;
    private final static Option sourceOption;
    private final static Option sumfileOption;
    private final static Option prefixOption;
    private final static Option localOption;
    private final static Option remoteOption;
    private final static Option pillarOption;
    private final static Option destinationOption;
    private final static Option typeOption;
    private final static Option helpOption;
    
    public final static String ACTION_OPT = "a";
    public final static String COLLECTION_OPT = "c";
    public final static String SOURCE_OPT = "s";
    public final static String SUMFILE_OPT = "f";
    public final static String PREFIX_OPT = "x";
    public final static String PILLAR_OPT = "p";
    public final static String DESTINATION_OPT = "d";
    public final static String TYPE_OPT = "t";
    public final static String HELP_OPT = "h";
    public final static String LOCAL_PREFIX_OPT = "l";
    public final static String REMOTE_PREFIX_OPT = "r";
    
    
    static {
        actionOption = new Option(ACTION_OPT, "action", true, "Action to perform");
        actionOption.setRequired(true);
        collectionOption = new Option(COLLECTION_OPT, "collection", true, "Collection to work on");
        sourceOption = new Option(SOURCE_OPT, "source", true, "Source directory to get files from");
        sumfileOption = new Option(SUMFILE_OPT, "sumfile", true, "Sumfile containing list of files to work on");
        prefixOption = new Option(PREFIX_OPT, "prefix", true, "Prefix for operations");
        localOption = new Option(LOCAL_PREFIX_OPT, "prefix", true, "Local prefix for operations");
        remoteOption = new Option(REMOTE_PREFIX_OPT, "prefix", true, "Remote prefix for operations");
        pillarOption = new Option(PILLAR_OPT, "pillar", true, "Pillar to perform delete on");
        destinationOption = new Option(DESTINATION_OPT, "destination", true, "Destination directory to place files in");
        typeOption = new Option(TYPE_OPT, "type", true, "Type of listing");
        helpOption = new Option(HELP_OPT, "help", false, "Prints help and usage information");
    }
    
    
    public static void printHelp(String scriptName, Options options) {
        String header = "Batch client to work with bitrepository.org bitrepository\n\n";
        String footer = "\nPlease report issues at https://github.com/statsbiblioteket/sb-bitrepository-client";
        
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(scriptName, header, options, footer, true);
    }
    
    public static Options getAllOptions() {
        Options options = new Options();
        options.addOption((Option) actionOption.clone());
        options.addOption((Option) collectionOption.clone());
        options.addOption((Option) sourceOption.clone());
        options.addOption((Option) sumfileOption.clone());
        options.addOption((Option) prefixOption.clone());
        options.addOption((Option) localOption.clone());
        options.addOption((Option) remoteOption.clone());
        options.addOption((Option) pillarOption.clone());
        options.addOption((Option) destinationOption.clone());
        options.addOption((Option) typeOption.clone());
        options.addOption((Option) helpOption.clone());
        return options;
    }
    
    public static Options getActionOptions(Action action) {
        Options options = new Options();
        options.addOption((Option) helpOption.clone());
        options.addOption((Option) actionOption.clone());
        
        switch(action) {
        case MAKECHECKUSMS:
            options.addOption(makeOptionRequired(sourceOption));
            options.addOption(makeOptionRequired(sumfileOption));
            break;
        case UPLOAD: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption((Option) localOption.clone());
            options.addOption((Option) remoteOption.clone());
            break;
        case LIST: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(typeOption));
            options.addOption((Option) prefixOption.clone());
            break;
        case DOWNLOAD: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(destinationOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption((Option) prefixOption.clone());
            break; 
        case DELETE:
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(pillarOption));
            options.addOption(makeOptionRequired(sumfileOption));
            break;
        }
        
        return options;
    }
    
    private static Option makeOptionRequired(Option opt) {
        Option optClone = (Option) opt.clone();
        optClone.setRequired(true);
        return optClone;
    }
}
