package dk.statsbiblioteket.bitrepository.commandline;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.statsbiblioteket.bitrepository.commandline.action.ClientAction;
import dk.statsbiblioteket.bitrepository.commandline.action.DeleteAction;
import dk.statsbiblioteket.bitrepository.commandline.action.DownloadAction;
import dk.statsbiblioteket.bitrepository.commandline.action.ListAction;
import dk.statsbiblioteket.bitrepository.commandline.action.MakeChecksumsAction;
import dk.statsbiblioteket.bitrepository.commandline.action.UploadAction;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;

public class Commandline {

    private final static String SCRIPT_NAME_PROPERTY = "sbclient.script.name"; 
    private final static String CONFIG_DIR_PROPERTY = "sbclient.config.dir";
    private final static String CLIENT_CERTIFICATE_FILE = "client-certificate.pem";
    
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
        String scriptName = System.getProperty(SCRIPT_NAME_PROPERTY);
        Path configDir = Paths.get(System.getProperty(CONFIG_DIR_PROPERTY));
        Path clientCertificate = configDir.resolve(CLIENT_CERTIFICATE_FILE);
        BitmagUtils.initialize(configDir, clientCertificate);
        try {
            
            CommandLine cmd = parser.parse(options, args);
            Action action = Action.fromString(cmd.getOptionValue(CliOptions.ACTION_OPT));
            if(action != null) {
                if(cmd.hasOption(CliOptions.HELP_OPT)) {
                    CliOptions.printHelp(scriptName, CliOptions.getActionOptions(action));
                    System.exit(0);
                }

                ClientAction ca = null;
                try {
                    cmd = parser.parse(CliOptions.getActionOptions(action), args);    
                } catch (MissingOptionException e) {
                    CliOptions.printHelp(scriptName, CliOptions.getActionOptions(action));
                    System.exit(2);
                } 
                switch(action) {
                case MAKECHECKUSMS:
                    ca = new MakeChecksumsAction(cmd);
                    break;
                case UPLOAD:
                    ca = new UploadAction(cmd, BitmagUtils.getPutFileClient(), BitmagUtils.getFileExchange());
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
            CliOptions.printHelp(scriptName, CliOptions.getAllOptions());
            System.exit(2);
        } catch (MissingArgumentException e) {
            CliOptions.printActionHelp(scriptName);
            System.exit(2);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
