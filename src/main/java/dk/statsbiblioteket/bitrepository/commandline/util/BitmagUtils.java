package dk.statsbiblioteket.bitrepository.commandline.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.CommandLineSettingsProvider;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;

/**
 * Utility class to abstract away the specifics of setting up and obtaining bitrepository.org clients.  
 */
public class BitmagUtils {
    
    private static Settings settings;
    private static SecurityManager securityManager;
    private static Path certificate;
    private static Path configDir;
       
    /**
     * Method to initialize the utility class. Must be called prior to use of any other method 
     * as it initializes internal state.
     * @param configurationDir Path to the configuration base directory, this is the directory where 
     * RepositorySettings.xml and ReferenceSettings.xml is expected to be found. 
     * @param clientCertificate Path to the clients certificate.   
     */
    public static void initialize(Path configurationDir, Path clientCertificate) {
        configDir = configurationDir;
        certificate = clientCertificate;
        settings = loadSettings(configDir);
        securityManager = loadSecurityManager(); 
    }
    
    private static Settings loadSettings(Path configurationDir) {
        SettingsProvider settingsLoader =
                new CommandLineSettingsProvider(new XMLFileSettingsLoader(configurationDir.toString()));
        Settings settings = settingsLoader.getSettings();
        SettingsUtils.initialize(settings);
        return settings;
    }
    
    private static SecurityManager loadSecurityManager() {
        ArgumentValidator.checkNotNull(settings, "Settings settings");
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(settings.getRepositorySettings(), certificate.toString(),
                authenticator, signer, authorizer, permissionStore,
                settings.getComponentID());
    }
    
    public static URL getFileExchangeBaseURL() throws MalformedURLException {
        FileExchangeSettings feSettings = settings.getReferenceSettings().getFileExchangeSettings();
        return new URL(feSettings.getProtocolType().value(), feSettings.getServerName(), 
                feSettings.getPort().intValue(), feSettings.getPath() + "/");
    }
    
    public static FileExchange getFileExchange() {
        return ProtocolComponentFactory.getInstance().getFileExchange(settings);
    }
    
    public static ChecksumDataForFileTYPE getChecksum(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        checksumData.setCalculationTimestamp(CalendarUtils.getNow());
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        checksumData.setChecksumSpec(checksumSpec);
        return checksumData;
    }
    
    /**
     * Retreive a PutFileClient
     */
    public static PutFileClient getPutFileClient() {
        return ModifyComponentFactory.getInstance().retrievePutClient(settings, 
                securityManager, settings.getComponentID());
    }

    /**
     * Retreive a DeleteFileClient
     */
    public static DeleteFileClient getDeleteFileClient() {
        return ModifyComponentFactory.getInstance().retrieveDeleteFileClient(settings, 
                securityManager, settings.getComponentID());
    }
    
    /**
     * Retreive a GetChecksumsClient
     */
    public static GetChecksumsClient getChecksumsClient() {
        return AccessComponentFactory.getInstance().createGetChecksumsClient(settings, 
                securityManager, settings.getComponentID());
    }
    
    /**
     * Retreive a GetFileIDsClient
     */
    public static GetFileIDsClient getFileIDsClient() {
        return AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager,
                settings.getComponentID());
    }
    
    /**
     * Retreive a GetFileClient
     */
    public static GetFileClient getFileClient() {
        return AccessComponentFactory.getInstance().createGetFileClient(settings, securityManager, 
                settings.getComponentID());
    }
}
