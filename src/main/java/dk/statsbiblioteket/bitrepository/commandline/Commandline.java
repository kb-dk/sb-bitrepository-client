package dk.statsbiblioteket.bitrepository.commandline;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Commandline {

    public static enum Action {
        MAKECHECKUSMS("makechecksums"), 
        UPLOAD("upload"),
        LIST("list"), 
        DOWNLOAD("download"),
        DELETE("delete");
        
        private String action;
        
        Action(String action) {
            this.action = action;
        }
        
        public String toString() {
            return action;
        }
        
        public static Action fromString(String action) {
            if (action != null) {
              for (Action a : Action.values()) {
                if (action.equalsIgnoreCase(a.action)) {
                  return a;
                }
              }
            }
            return null;
          }
        
    }
    
    public static void main(String[] args) throws ParseException {
        System.out.println("Hello world");
        System.out.println("myargs:" + Arrays.asList(args));
        Action foo = Action.MAKECHECKUSMS;
        Action bar = Action.fromString("upload");
        System.out.println("foo: " + foo.toString() + ", bar: " + bar.toString());
        Options options = CliOptions.getAllOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            
            CommandLine cmd = parser.parse(options, args);
            Action action = Action.fromString(cmd.getOptionValue("a"));
            if(action == null) {
                System.out.println("Unknown action");
                System.exit(1);
            }
            if(cmd.hasOption("h")) {
                CliOptions.printHelp(CliOptions.getActionOptions(action));
            }
        } catch (MissingOptionException e) {
            CliOptions.printHelp(CliOptions.getAllOptions());
        }
    }

}
