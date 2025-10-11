package no.avandra.classes;

public interface DBHandler {
    public void sendData(String key, Object object);
    public void appendData(String id, String addKey, String addValue);

    /// returns arraylist in mongo, object in json. COULD both be arraylist though pointless for json
    public Object retrieveAllData();
}
