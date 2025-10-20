package avandra.core.port;

import avandra.core.domain.Coordinate;

public interface DBHandler {
    public void sendData(String key, Object object);

    /// @param addValue is Object to allow appending Documents, ArrayLists and Strings
    public void appendData(String id, String addKey, Object addValue);

    /// returns arraylist in mongo, object in json. COULD both be arraylist though pointless for json
    public Object retrieveAllData();

    ///  Returnerer koordinater
    public Coordinate destinationCoordinate(String name);


}

