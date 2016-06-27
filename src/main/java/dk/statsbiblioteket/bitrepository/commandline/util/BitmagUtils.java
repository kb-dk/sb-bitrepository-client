package dk.statsbiblioteket.bitrepository.commandline.util;

import java.nio.file.Path;

import org.bitrepository.client.CommandLineSettingsProvider;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;

public class BitmagUtils {
    
    private static Settings settings;
        
    public static Settings loadSettings(Path configurationDir) {
        if(settings == null) {
            SettingsProvider settingsLoader =
                new CommandLineSettingsProvider(new XMLFileSettingsLoader(configurationDir.toString()));
            settings = settingsLoader.getSettings();
        }
        SettingsUtils.initialize(settings);
        return settings;
    }
    
    public BasicSecurityManager loadSecurityManager(Settings settings, Path clientCertificate) {
        ArgumentValidator.checkNotNull(settings, "Settings settings");
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(settings.getRepositorySettings(), clientCertificate.toString(),
                authenticator, signer, authorizer, permissionStore,
                settings.getComponentID());
    }
}
