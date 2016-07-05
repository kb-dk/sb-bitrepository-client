package dk.statsbiblioteket.bitrepository.commandline.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.CliOptions;


public class MakeChecksumsActionTest {

    @Test
    public void testGeneration() throws IOException {
        String sourceDir = "src/test/resources/testdir/";
        String outputSumFile = "target/MakeChecksumsActionTest-testGeneration";
        Path outputPath = Paths.get(outputSumFile);       
        outputPath.toFile().deleteOnExit();
        
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.getOptionValue(CliOptions.SOURCE_OPT)).thenReturn(sourceDir);
        when(cmd.getOptionValue(CliOptions.SUMFILE_OPT)).thenReturn(outputSumFile);
        
        Assert.assertTrue(Files.exists(Paths.get(sourceDir)));
        Assert.assertTrue(Files.notExists(Paths.get(outputSumFile)));
        
        MakeChecksumsAction action = new MakeChecksumsAction(cmd);
        action.performAction();
        
        Assert.assertTrue(Files.exists(Paths.get(outputSumFile)));
        
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(outputSumFile), charset)) {
            String line = null;
            line = reader.readLine();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, "08aae96be03b06a9e69bb4795188b3b5  src/test/resources/testdir/testfile1");

            line = reader.readLine();
            Assert.assertNull(line);
        }
    }
}
