package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     */
    public MakeChecksumsAction(CommandLine cmd) {
        baseDir = Paths.get(cmd.getOptionValue(CliOptions.SOURCE_OPT));
        sumfile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
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
