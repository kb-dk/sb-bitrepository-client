package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;
import dk.statsbiblioteket.bitrepository.commandline.util.FileIDTranslationUtil;
import dk.statsbiblioteket.bitrepository.commandline.util.SkipFileException;

public class DownloadAction implements ClientAction {

    private final String collectionID;
    private final Path sumFile;
    private String localPrefix = null;
    private String remotePrefix = null;
    
    public DownloadAction(CommandLine cmd) {
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        sumFile = Paths.get(cmd.getOptionValue(CliOptions.SUMFILE_OPT));
        if(cmd.hasOption(CliOptions.LOCAL_PREFIX_OPT)) {
            localPrefix = cmd.getOptionValue(CliOptions.LOCAL_PREFIX_OPT);
        }
        if(cmd.hasOption(CliOptions.REMOTE_PREFIX_OPT)) {
            remotePrefix = cmd.getOptionValue(CliOptions.REMOTE_PREFIX_OPT);
        }

    }
    
    public void performAction() {
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(sumFile, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("  ");
                String origRemote = parts[1];
                
                String localFilename;
                try {
                    localFilename = FileIDTranslationUtil.remoteToLocal(origRemote, localPrefix, remotePrefix);
                } catch (SkipFileException e) {
                    System.out.println("[SKIPPING]: " + origRemote);
                    continue;
                }
                
                System.out.println("Getting '" + origRemote +"' as '" + localFilename + "'");
                }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

    }

}
