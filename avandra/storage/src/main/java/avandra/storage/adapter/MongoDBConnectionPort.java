package avandra.storage.adapter;

import avandra.core.port.DBConnectionPort;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDBConnectionPort implements AutoCloseable, DBConnectionPort {

    private String user;
    private String pass;
    private String uri;                  // full connection string path
    private String db_name = "dummy";
    private String collection_name = "testdata";
    private MongoClient client;

    /// Exists only for developers, will not go to launch
    public MongoDBConnectionPort() {
        this.user = "siljemst_db_user";
        this.pass = "Avandra1234567890";
        this.db_name = getDbName();
        this.collection_name = getCollectionName();
    }

    /// For users and system administrators
    public MongoDBConnectionPort(String username, String password) {
        this.user = username;
        this.pass = password;
        this.db_name = getDbName();
        this.collection_name = getCollectionName();
    }

    /// For Testcontainers or any full connection string
    public MongoDBConnectionPort(String connectionString, String dbName, String collectionName) {
        this.uri = connectionString;        // <-- store URI from container (or Atlas)
        this.db_name = dbName;
        this.collection_name = collectionName;
    }

    public String getUser() { return user; }
    public String getPass() { return pass; }
    public String getDbName() { return db_name; }
    public String getCollectionName() { return collection_name; }

    public void setCollectionName(String collection_name) { this.collection_name = collection_name; }
    public void setDbName(String db_name) { this.db_name = db_name; }

    public MongoCollection<Document> getCollection() {
        return client.getDatabase(getDbName()).getCollection(getCollectionName());
    }

    /// Returns self as an opened connection to the database
    public MongoDBConnectionPort open() throws Exception {
        if (uri != null && !uri.isBlank()) {
            // Use explicit connection string (e.g., from Testcontainers or Atlas URI env var)
            this.client = MongoClients.create(uri);
        } else {
            // Backward-compatible: build Atlas SRV from user/pass
            this.client = MongoClients.create(
                    "mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/db"
            );
        }
        return this;
    }

    @Override
    public void close() throws Exception {
        if (client != null) client.close();
    }
}
