package avandra.core.port;

import avandra.core.domain.Coordinate;

import java.util.List;

public interface DBHandler {
    // make user in DB %overloading skal brukes%
    public void createUser( String userID, boolean adminUser, List<String> liteUsers);
    public void createUser( String userID, boolean adminUser, String age, List<String> liteUsers, String favoriteDestination, String address);
    public void createUser( String userID, boolean adminUser, String age, List<String> liteUsers, String favoriteDestination, String address, double latitude, double longitude);


    /// @param addValue is Object to allow appending Documents, ArrayLists and Strings
    public void appendData(String id, String addKey, Object addValue);

    /// returns arraylist in mongo, object in json. COULD both be arraylist though pointless for json
    public Object retrieveAllData();

    ///  Returnerer koordinater
    public Coordinate destinationCoordinate(String name);

    /// Legger til koordinater i lagret destinasjon
    public void addCoordinatesToDestination(String userID, String destinationName, double latitude, double longitude);

    //search with ID to find specific document
    public Coordinate searchDestination(String userID, String destinationType, String destinationID);

    //Removes data (dkument id, what to delete, path, path, path) %overloading skal brukes%
    public void removeData(String userID); //sletter topp nivå i dokument
    public void removeData(String userID, String keyToRemove); //sletter topp nivå i dokument
    public void removeData(String userID, String keyToRemove, String destinationType); //sletter element i array
    public void removeData(String userID, String keyToRemove, String destinationType, String destinationKey); //sletter kordinater


    //admin gir lite destinasjoner retunerer bra eller dårlig
    public boolean insertDestinationForLiteUser(String liteUserId,
            String destId,
            String name,
            String address,
            Double lat,
            Double lng,
            String adminId
            );
}

