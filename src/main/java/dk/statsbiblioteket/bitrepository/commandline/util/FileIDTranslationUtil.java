package dk.statsbiblioteket.bitrepository.commandline.util;

/**
 * Utility class to handle translations between remote and local files.  
 */
public class FileIDTranslationUtil {

    /**
     * Translate local FileID to remote fileID
     * @param localFileID The id of the file locally
     * @param localPrefix The local prefix, may be null
     * @param remotePrefix The remote prefix, may be null
     * @return remoteFileID The file ID as it should be remotely.  
     */
    public static String localToRemote(String localFileID, String localPrefix, String remotePrefix) throws SkipFileException {
        String stripped;
        if(localPrefix != null) {
            if(localFileID.startsWith(localPrefix)) {
                stripped = localFileID.substring(localPrefix.length());
            } else {
                throw new SkipFileException("Local prefix provided, but not found in local file");
            }
        } else {
            stripped = localFileID;
        }
        
        String remoteFileID;
        if(remotePrefix != null) {
            remoteFileID = remotePrefix + stripped;
        } else {
            remoteFileID = stripped;
        }
        
        //return remoteFileID; //Temporary to ensure that the file can be stored in a SB Pillar
        return remoteFileID.replace("/", "\\");
    }
    
    /**
     * Translate remote FileID to local fileID
     * @param remoteFileID The id of the file remotely
     * @param localPrefix The local prefix, may be null
     * @param remotePrefix The remote prefix, may be null
     * @return localFileID The file ID as it should be locally.  
     */    
    public static String remoteToLocal(String remoteFileID, String localPrefix, String remotePrefix) throws SkipFileException {
        String stripped;
        remoteFileID = remoteFileID.replace("\\", "/"); // Temporary measure to ensure that files can handled on SB pillar
        if(remotePrefix != null) {
            if(remoteFileID.startsWith(remotePrefix)) {
               stripped = remoteFileID.substring(remotePrefix.length());
            } else {
                throw new SkipFileException("Remote prefix provided, but not found in remote file");
            }
        } else {
            stripped = remoteFileID;
        }
        
        String localFileID;
        if(localPrefix != null) {
            localFileID = localPrefix + stripped;
        } else {
            localFileID = stripped;
        }
        
        return localFileID;
    }
}
