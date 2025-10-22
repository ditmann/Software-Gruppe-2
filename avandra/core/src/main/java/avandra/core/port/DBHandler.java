package avandra.core.port;

import avandra.core.domain.Coordinate;

public interface DBHandler {
    // make user in DB
    public void createUser(String key, Object object);

    /// @param addValue is Object to allow appending Documents, ArrayLists and Strings
    public void appendData(String id, String addKey, Object addValue);

    /// returns arraylist in mongo, object in json. COULD both be arraylist though pointless for json
    public Object retrieveAllData();

    ///  Returnerer koordinater
    public Coordinate destinationCoordinate(String name);

    //search with ID to find specific document
    public Object searchDestination(String userID, String destinationType, String destinationID);

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

