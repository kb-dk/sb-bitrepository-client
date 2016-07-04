package dk.statsbiblioteket.bitrepository.commandline.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FileIDTranslationUtilTest {

    // FIXME When directory support is implemented on SBPillar, fix tests so they'll stop translating / into \
    
    private final static String TEST_FILEID = "foo/lib/jcl-over-slf4j-1.7.14.jar";
    
    @Test
    public void testLocalToRemoteNoPrefix() throws SkipFileException {
        String expectedOutput = "foo\\lib\\jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.localToRemote(TEST_FILEID, null, null);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test
    public void testLocalToRemoteLocalPrefix() throws SkipFileException {
        String localPrefix = "foo/";
        String expectedOutput = "lib\\jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.localToRemote(TEST_FILEID, localPrefix, null);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test
    public void testLocalToRemoteRemotePrefix() throws SkipFileException {
        String remotePrefix = "foo/";
        String expectedOutput = "foo\\foo\\lib\\jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.localToRemote(TEST_FILEID, null, remotePrefix);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test
    public void testLocalToRemoteLocalAndRemotePrefix() throws SkipFileException {
        String localPrefix = "foo/";
        String remotePrefix = "bar/";
        String expectedOutput = "bar\\lib\\jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.localToRemote(TEST_FILEID, localPrefix, remotePrefix);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test(expectedExceptions = {SkipFileException.class})
    public void testLocalToRemoteSkipfile() throws SkipFileException {
        String localPrefix = "bar/";
        String translatedFileID = FileIDTranslationUtil.localToRemote(TEST_FILEID, localPrefix, null);
    }
    
    @Test
    public void testRemoteToLocalNoPrefix() throws SkipFileException {
        String expectedOutput = "foo/lib/jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.remoteToLocal(TEST_FILEID, null, null);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test
    public void testRemoteToLocalLocalPrefix() throws SkipFileException {
        String localPrefix = "foo/";
        String expectedOutput = "foo/foo/lib/jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.remoteToLocal(TEST_FILEID, localPrefix, null);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test
    public void testRemoteToLocalRemotePrefix() throws SkipFileException {
        String remotePrefix = "foo/";
        String expectedOutput = "lib/jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.remoteToLocal(TEST_FILEID, null, remotePrefix);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test
    public void testRemoteToLocalLocalAndRemotePrefix() throws SkipFileException {
        String localPrefix = "bar/";
        String remotePrefix = "foo/";
        String expectedOutput = "bar/lib/jcl-over-slf4j-1.7.14.jar";
        
        String translatedFileID = FileIDTranslationUtil.remoteToLocal(TEST_FILEID, localPrefix, remotePrefix);
        Assert.assertEquals(translatedFileID, expectedOutput);
    }
    
    @Test(expectedExceptions = {SkipFileException.class})
    public void testRemoteToLocalSkipfile() throws SkipFileException {
        String remotePrefix = "bar/";
        String translatedFileID = FileIDTranslationUtil.remoteToLocal(TEST_FILEID, null, remotePrefix);
    }
    
}
