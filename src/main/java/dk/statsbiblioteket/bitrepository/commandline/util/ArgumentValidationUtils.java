package dk.statsbiblioteket.bitrepository.commandline.util;

import java.util.List;

public class ArgumentValidationUtils {

    /**
     * Method to validate that the given collection is in the RepositorySettings
     * @param collectionID The collectionID to validate
     * @throws InvalidParameterException if the collectionID is not found in the RepositorySettings 
     */
    public static void validateCollection(String collectionID) throws InvalidParameterException {
        List<String> knownCollections = BitmagUtils.getKnownCollections();
        if(!knownCollections.contains(collectionID)) {
            throw new InvalidParameterException("Invalid collection. Valid collections: " 
                    + knownCollections);
        }
    }

    /**
     * Method to validate that the given pillarID is found in the collection in the RepositorySettings
     * @param pillarID The pillarID to validate
     * @param collectionID The collectionID to look for the pillarID
     * @throws InvalidParameterException if the pillarID is not present in the collection in the RepositorySettings 
     */
    public static void validatePillar(String pillarID, String collectionID) throws InvalidParameterException {
        List<String> knownPillars = BitmagUtils.getKnownPillars(collectionID);
        if(!knownPillars.contains(pillarID)) {
            throw new InvalidParameterException("Invalid pillar for collection '" + collectionID 
                    + "'. Valid pillars: " + knownPillars);
        }
    }
}
