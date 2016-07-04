package dk.statsbiblioteket.bitrepository.commandline.util;

import java.util.List;

public class ArgumentValidationUtils {

    
    public static void validateCollection(String collectionID) throws InvalidParameterException {
        List<String> knownCollections = BitmagUtils.getKnownCollections();
        if(!knownCollections.contains(collectionID)) {
            throw new InvalidParameterException("Invalid collection. Valid collections: " 
                    + knownCollections);
        }
    }
    
    public static void validatePillar(String pillarID, String collectionID) throws InvalidParameterException {
        List<String> knownPillars = BitmagUtils.getKnownPillars(collectionID);
        if(!knownPillars.contains(pillarID)) {
            throw new InvalidParameterException("Invalid pillar for collection '" + collectionID 
                    + "'. Valid pillars: " + knownPillars);
        }
    }
}
