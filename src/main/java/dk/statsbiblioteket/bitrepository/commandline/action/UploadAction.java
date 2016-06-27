package dk.statsbiblioteket.bitrepository.commandline.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;

public class UploadAction implements ClientAction {

    private final String collectionId;
    private final Path sumFile;
    private String localPrefix = null;
    private String remotePrefix = null;
    
    public UploadAction(CommandLine cmd) {
        collectionId = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
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
                String origFilename = parts[1];
                String checksum = parts[0];
                
                String strippedLocal;
                if(localPrefix != null) {
                    if(origFilename.startsWith(localPrefix)) {
                        strippedLocal = origFilename.replaceFirst(localPrefix, "");
                    } else {
                        // log the mismatch, and skip it
                        continue;
                    }
                }
                 else {
                    strippedLocal = origFilename;
                }
                
                String remoteFilename;
                if(remotePrefix != null) {
                    remoteFilename = remotePrefix + strippedLocal;
                } else {
                    remoteFilename = strippedLocal;
                }
                
                System.out.println("OrigFilename: '" + origFilename +"', stripped: '" + strippedLocal + "', remote: '" + remoteFilename + "'");
                }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
        

    }

}
