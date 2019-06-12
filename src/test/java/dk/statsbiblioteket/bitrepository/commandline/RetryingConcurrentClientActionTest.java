package dk.statsbiblioteket.bitrepository.commandline;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.bitrepository.commandline.util.MD5SumFileWriter;

public class RetryingConcurrentClientActionTest {

    @Test
    public void testLineParsing() {
        String line = "08124e34e05b3a260b2b8b375474c0a3  file with two  spaces in name.xml";
        String[] parts = line.split(MD5SumFileWriter.MD5_FILE_FIELD_SEPERATOR);
        String checksum = parts[0];
        String origFilename = line.substring(checksum.length() + MD5SumFileWriter.MD5_FILE_FIELD_SEPERATOR.length());
       
        Assert.assertEquals(checksum, "08124e34e05b3a260b2b8b375474c0a3");
        Assert.assertEquals(origFilename, "file with two  spaces in name.xml");
        
    }
}
