package dk.statsbiblioteket.bitrepository.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.statsbiblioteket.bitrepository.commandline.action.ClientAction;
import dk.statsbiblioteket.bitrepository.commandline.action.DeleteAction;
import dk.statsbiblioteket.bitrepository.commandline.action.DownloadAction;
import dk.statsbiblioteket.bitrepository.commandline.action.ListAction;
import dk.statsbiblioteket.bitrepository.commandline.action.MakeChecksumsAction;
import dk.statsbiblioteket.bitrepository.commandline.action.UploadAction;

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
        Options options = CliOptions.getAllOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            
            CommandLine cmd = parser.parse(options, args);
            Action action = Action.fromString(cmd.getOptionValue(CliOptions.ACTION_OPT));
            if(action != null) {
                if(cmd.hasOption(CliOptions.HELP_OPT)) {
                    CliOptions.printHelp(CliOptions.getActionOptions(action));
                    System.exit(0);
                }

                ClientAction ca = null;
                try {
                    cmd = parser.parse(CliOptions.getActionOptions(action), args);    
                } catch (MissingOptionException e) {
                    CliOptions.printHelp(CliOptions.getActionOptions(action));
                    System.exit(2);
                } 
                switch(action) {
                case MAKECHECKUSMS:
                    ca = new MakeChecksumsAction(cmd);
                    break;
                case UPLOAD:
                    ca = new UploadAction(cmd);
                    break;
                case LIST: 
                    ca = new ListAction(cmd);
                    break;
                case DOWNLOAD:
                    ca = new DownloadAction(cmd);
                    break;
                case DELETE:
                    ca = new DeleteAction(cmd);
                    break;
                default:
                    throw new RuntimeException("Unknown action: '" + action + "'");
                }
                
                ca.performAction();
            }
        } catch (MissingOptionException e) {
            CliOptions.printHelp(CliOptions.getAllOptions());
            System.exit(2);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
