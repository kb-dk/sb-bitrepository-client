package dk.statsbiblioteket.bitrepository.commandline;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.jms.JMSException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.bitrepository.commandline.action.ClientAction;
import dk.statsbiblioteket.bitrepository.commandline.action.DeleteAction;
import dk.statsbiblioteket.bitrepository.commandline.action.DownloadAction;
import dk.statsbiblioteket.bitrepository.commandline.action.ListAction;
import dk.statsbiblioteket.bitrepository.commandline.action.MakeChecksumsAction;
import dk.statsbiblioteket.bitrepository.commandline.action.UploadAction;
import dk.statsbiblioteket.bitrepository.commandline.util.BitmagUtils;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;
import dk.statsbiblioteket.bitrepository.commandline.util.StatusReporter;

/**
 * Main class for running the SB-bitrepository client 
 */
public class Commandline {

    final static Logger log = LoggerFactory.getLogger(Commandline.class);
    private final static String SCRIPT_NAME_PROPERTY = "sbclient.script.name"; 
    private final static String CONFIG_DIR_PROPERTY = "sbclient.config.dir";
    private final static String CLIENT_CERTIFICATE_FILE = "client-certificate.pem";
    
    /**
     * Action types for the available actions.  
     */
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
        int exitStatus = 0;
        String scriptName = System.getProperty(SCRIPT_NAME_PROPERTY);
        Path configDir = Paths.get(System.getProperty(CONFIG_DIR_PROPERTY));
        Path clientCertificate = configDir.resolve(CLIENT_CERTIFICATE_FILE);
        BitmagUtils.initialize(configDir, clientCertificate);
        try {
            Options options = CliOptions.getAllOptions();
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            Action action = Action.fromString(cmd.getOptionValue(CliOptions.ACTION_OPT));
            if(action == null) {
                CliOptions.printActionHelp(scriptName);
                System.exit(2);
            }
            if(cmd.hasOption(CliOptions.HELP_OPT)) {
                CliOptions.printHelp(scriptName, CliOptions.getActionOptions(action));
                System.exit(0);
            }

            ClientAction ca = null;
            try {
                // Parsing a second time to get action specific options.
                cmd = parser.parse(CliOptions.getActionOptions(action), args);    
            } catch (MissingOptionException e) {
                CliOptions.printHelp(scriptName, CliOptions.getActionOptions(action));
                System.exit(2);
            } 
            try {
                switch(action) {
                case MAKECHECKUSMS:
                    ca = new MakeChecksumsAction(cmd);
                    break;
                case UPLOAD:
                    ca = new UploadAction(cmd, BitmagUtils.getPutFileClient(), BitmagUtils.getFileExchange(),
                            new StatusReporter(System.err, Action.UPLOAD));
                    break;
                case LIST: 
                    ca = new ListAction(cmd, BitmagUtils.getChecksumsClient());
                    break;
                case DOWNLOAD:
                    ca = new DownloadAction(cmd, BitmagUtils.getFileClient(), BitmagUtils.getFileExchange(),
                            new StatusReporter(System.err, Action.DOWNLOAD));
                    break;
                case DELETE:
                    ca = new DeleteAction(cmd, BitmagUtils.getDeleteFileClient(), 
                            new StatusReporter(System.err, Action.DELETE));
                    break;
                default:
                    throw new RuntimeException("Unknown action: '" + action + "'");
                }
                
                ca.performAction();
            } finally {
                try {
                    BitmagUtils.shutdown();
                } catch (JMSException e) {
                    log.error("Caught an error shutting down bitrepository", e);
                    System.err.println(e.getMessage());
                    exitStatus = 1;
                }
            }
        } catch (MissingOptionException e) {
            CliOptions.printHelp(scriptName, CliOptions.getAllOptions());
            exitStatus = 2;
        } catch (MissingArgumentException e) {
            CliOptions.printActionHelp(scriptName);
            exitStatus = 2;
        } catch (InvalidParameterException e) {
            System.err.println(e.getMessage());
            exitStatus = 3;
        } catch (RuntimeException e) {
            Commandline.log.error("Caught RuntimeException", e);
            System.err.println(e.getMessage());
            exitStatus = 1;
        }
        
        System.exit(exitStatus);
    }

}
