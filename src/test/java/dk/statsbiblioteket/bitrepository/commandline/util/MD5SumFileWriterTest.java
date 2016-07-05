package dk.statsbiblioteket.bitrepository.commandline.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MD5SumFileWriterTest {

    
    @Test
    public void testFileWriting() throws IOException {
        Path currentDir = Paths.get("target/MD5SumFileWriterTest-testFileWriting");
        currentDir.toFile().deleteOnExit();
        String checksum1 = "abab";
        String checksum2 = "bcbc";
        String fileID1 = "foo";
        String fileID2 = "bar";
        
        String expectedLine1 = checksum1 + "  " + fileID1;
        String expectedLine2 = checksum2 + "  " + fileID2;
        
        Assert.assertTrue(Files.notExists(currentDir));
        
        try (MD5SumFileWriter writer = new MD5SumFileWriter(currentDir)) {
            writer.writeChecksumLine(Paths.get(fileID1), checksum1);
            writer.writeChecksumLine(Paths.get(fileID2), checksum2);
        }
        
        Assert.assertTrue(Files.exists(currentDir));
        
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(currentDir, charset)) {
            String line = null;
            line = reader.readLine();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, expectedLine1);

            line = reader.readLine();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, expectedLine2);

            line = reader.readLine();
            Assert.assertNull(line);
        }
        
    }
    
    @Test(expectedExceptions = {RuntimeException.class})
    public void testFailureWithExistingFile() throws IOException {
        Path currentDir = Paths.get("target/MD5SumFileWriterTest-testFailureWithExistingFile");
        Files.createFile(currentDir);
        currentDir.toFile().deleteOnExit();
        
        MD5SumFileWriter writer = new MD5SumFileWriter(currentDir);
    }
}
