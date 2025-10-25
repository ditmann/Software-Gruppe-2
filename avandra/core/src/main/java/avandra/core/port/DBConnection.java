package avandra.core.port;

public interface DBConnection {

    public Object open() throws Exception;
    public void close() throws Exception;

}
