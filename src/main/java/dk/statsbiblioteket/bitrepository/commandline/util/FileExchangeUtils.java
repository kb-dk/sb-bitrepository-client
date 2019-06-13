package dk.statsbiblioteket.bitrepository.commandline.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.bitrepository.protocol.FileExchange;

import java.net.MalformedURLException;
import java.net.URL;

public class FileExchangeUtils {
    
    /**
     * Method to create the URL to where the file should be placed on the {@link FileExchange}.
     * The URL's should be unique, but reproducible so as to help keep the {@link FileExchange} clean.
     */
    public static URL getUrl(String collectionID, String filename) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        String path = DigestUtils.sha1Hex(collectionID + filename);
        return new URL(baseurl.toString() + path);
    }
}
