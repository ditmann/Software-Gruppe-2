package no.avandra.classes;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;

public class MongoDBHandler implements DBHandler {

    /// MAKE:
    /// 1. append key:value-pair to existing doc
    /// 2. delete key:value-pair in existing doc
    /// 3. delete entire doc
    /// 4. search by loose value "kåre"

    /// TEST:
    /// 1. all....
    /// 2. return of retrieveAll
    /// 3. return of searchByKeyValue when more than 1 pair

    /// Creates a doc with the given content at the specified db and collection
    public void sendData(Object title, String content){
        /// for future use: take input?
        // find secure way to assign variables from front end (?) or store securely closer to core(?)
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(collection_name);

        /// insertion of param - actual use of funct
        collection.insertOne(new Document(title.toString(), content));

        /// DESTROY CONNECTION
        mongoClient.close();

    }

    /// Returns all documents in the collection as an array
    public ArrayList<Document> retrieveAllData() {
        /// Same vars
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(collection_name);

        /// Retrieval of data - actual use of funct
        FindIterable<Document> content = collection.find();
        ArrayList<Document> list = new ArrayList<>();
        for (Document doc : content) {
            list.add(doc);
        }

        /// DESTROY CONNECTION
        mongoClient.close();

        //to satisfy the declaration in interface
        return list;

    }

    /// Returns all docs which contain the specified key:value in an array
    public ArrayList<Document> searchByKeyValue(String key, String value){
        /// Same vars
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(collection_name);

        /// Retrieval of data - actual use of funct
        FindIterable<Document> content = collection.find(Filters.eq(key, value));
        ArrayList<Document> list = new ArrayList<>();
        for (Document doc : content) {
            list.add(doc);
        }

        /// DESTROY CONNECTION
        mongoClient.close();

        //to satisfy the declaration in interface
        return list;
    }

/*
 [
  {
    "id":"1",
    "user_id":"kare_demo",'
    "destinasjon":"Hjem",
    "adresse":"Dr. Ellertsens Vei",
    "latitudeNum":59.13555,
    "longitudeNUM":10.59498
  }
]
*/
}
