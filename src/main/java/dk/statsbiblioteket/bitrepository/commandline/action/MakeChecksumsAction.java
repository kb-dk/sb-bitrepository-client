package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5CalculatingFileVisitor;
import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;

public class MakeChecksumsAction implements ClientAction {

    private final Path sumfile;
    private final Path baseDir;
    
    public MakeChecksumsAction(CommandLine cmd) {
        baseDir = Paths.get(cmd.getOptionValue(CliOptions.SOURCE_OPT));
        sumfile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
    }
    
    public void performAction() {
        try (MD5SumFileWriter writer = new MD5SumFileWriter(sumfile)){
            MD5CalculatingFileVisitor visitor = new MD5CalculatingFileVisitor(writer);
            Files.walkFileTree(baseDir, visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
