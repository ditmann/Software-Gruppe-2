package avandra.core.port;

public interface DBConnection {

    /// Open connection through object
    public Object open() throws Exception;
    public void close() throws Exception;

}
