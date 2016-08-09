package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import dk.statsbiblioteket.bitrepository.commandline.util.InvalidParameterException;

import org.apache.commons.cli.CommandLine;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5CalculatingFileVisitor;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;

/**
 * Action to produce a sumfile from a local directory tree.
 * The produced sumfile is relative to the current directory, but only traverses the tree from the 
 * supplied base directory.  
 */
public class MakeChecksumsAction implements ClientAction {

    private final Path sumfile;
    private final Path baseDir;
    
    /**
     * Constructor for the action
     * @param cmd The {@link CommandLine} with parsed arguments
     * @throws InvalidParameterException if input fails validation
     */
    public MakeChecksumsAction(CommandLine cmd) throws InvalidParameterException {
        sumfile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        baseDir = Paths.get(cmd.getOptionValue(CliOptions.SOURCE_OPT));
        if(!Files.isDirectory(baseDir)) {
            if(Files.notExists(baseDir)) {
                throw new InvalidParameterException("Source directory '" + baseDir + "' does not exists");
            } else {
                throw new InvalidParameterException("Source directory '" + baseDir + "' is not a directory");
            }
        }
    }
 
    @Override
    public void performAction() {
        try (MD5SumFileWriter writer = new MD5SumFileWriter(sumfile)){
            MD5CalculatingFileVisitor visitor = new MD5CalculatingFileVisitor(writer);
            Files.walkFileTree(baseDir, visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
