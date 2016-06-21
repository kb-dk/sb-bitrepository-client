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
    private final static Option pillarOption;
    private final static Option destinationOption;
    private final static Option typeOption;
    private final static Option helpOption;
    
    static {
        actionOption = new Option("a", "action", true, "Action to perform");
        actionOption.setRequired(true);
        collectionOption = new Option("c", "collection", true, "Collection to work on");
        sourceOption = new Option("s", "source", true, "Source directory to get files from");
        sumfileOption = new Option("f", "sumfile", true, "Sumfile containing list of files to work on");
        prefixOption = new Option("x", "prefix", true, "Prefix for operations");
        pillarOption = new Option("p", "pillar", true, "Pillar to perform delete on");
        destinationOption = new Option("d", "destination", true, "Destination directory to place files in");
        typeOption = new Option("t", "type", true, "Type of listing");
        helpOption = new Option("h", "help", false, "Prints help and usage information");
    }
    
    
    public static void printHelp(Options options) {
        String header = "Batch client to work with bitrepository.org bitrepository\n\n";
        String footer = "\nPlease report issues at https://github.com/statsbiblioteket/sb-bitrepository-client";
        
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("sbclient", header, options, footer, true);
    }
    
    public static Options getAllOptions() {
        Options options = new Options();
        options.addOption((Option) actionOption.clone());
        options.addOption((Option) collectionOption.clone());
        options.addOption((Option) sourceOption.clone());
        options.addOption((Option) sumfileOption.clone());
        options.addOption((Option) prefixOption.clone());
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
            options.addOption(makeOptionRequired(sourceOption));
            options.addOption(makeOptionRequired(sumfileOption));
            options.addOption((Option) prefixOption.clone());
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
