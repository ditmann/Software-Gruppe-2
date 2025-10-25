package avandra.storage.adapter;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnection implements AutoCloseable {

    private final String user; //final because one must log out to log back in, restart is okay
    private final String pass; //^
    private String db_name = "dummy";
    private String collection_name = "testdata";
    private final MongoClient client;


/// Exists only for developers, will not go to launch
    public MongoDBConnection() {
        this.user = "siljemst_db_user";
        this.pass = "Avandra1234567890";
        this.db_name = getDbName();
        this.collection_name = getCollectionName();
        this.client = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");
    }

/// For users and system administrators
    public MongoDBConnection(String username, String password) {
        this.user = username;
        this.pass = password;
        this.db_name = getDbName();
        this.collection_name = getCollectionName();
        this.client = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDbName() {
        return db_name;
    }

    public String getCollectionName() {
        return collection_name;
    }

    /// makes it possible to open a different Database or different collection in MongoDB
    public void setCollectionName(String collection_name) {
        this.collection_name = collection_name;
    }
    public void setDbName(String db_name) {
        this.db_name = db_name;
    }


   /*public MongoDatabase getDb() {
        return client.getDatabase(getDbName());
   }
   we need?
   */

/// Gives access to the specified collection
//could have an override for use with other collections but this application
//has only one collection for all its data
   public MongoCollection<Document> getCollection () {
        return client.getDatabase(getDbName()).getCollection(getCollectionName());
   }

    public MongoDBConnection open() throws Exception {
        return this;
    }

    @Override //good to have
    public void close() throws Exception {
        client.close();
    }
}

