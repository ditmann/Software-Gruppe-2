package no.avandra.classes;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;

public class MongoDBHandler implements DBHandler {

    /// MAKE:
    /// 1. append key:value-pair to existing doc V
    /// 2. delete key:value-pair in existing doc V
    /// 3. delete entire doc V
    /// 4. search by loose value "kåre" V
    /// 5. error handling.
    /// 6. user error handling :  if no field that is id ? if ID already exists it currently makes duplicate

    /// TEST:
    /// 1. all....
    /// 2. return of retrieveAll
    /// 3. return of searchByKeyValue when more than 1 pair

    /// CHANGE:
    /// 1. If Document == class (Bruker) then:
    /// receive, convert, add variable to class - add item to list in class?, then convert back and send



    /// Creates a doc with the given content at the specified db and collection
    //TODO: prevent duplicates:
    //make it use appendData if id already exists? maybe just not work?
    public void sendData(String key, Object object){
        /// for future use: take input?
        // find secure way to assign variables from front end (?) or store securely closer to core(?)
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";
        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(db_name);
            MongoCollection<Document> collection = db.getCollection(collection_name);

            /// insertion of param - actual use of funct
            collection.insertOne(new Document(key, object.toString()));

            /// DESTROY CONNECTION
            mongoClient.close();
        }

        /// Super basic error "handling" + if mongo-specific, notification
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }

    }

    /// Returns all documents in the collection as an array
    public ArrayList<Document> retrieveAllData() {
        /// Same vars
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";
        ArrayList<Document> list = new ArrayList<>();

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(db_name);
            MongoCollection<Document> collection = db.getCollection(collection_name);

            /// Retrieval of data - actual use of funct
            FindIterable<Document> content = collection.find();
            for (Document doc : content) {
                list.add(doc);
            }

            /// DESTROY CONNECTION
            mongoClient.close();
        }

        /// Super basic error "handling" + if mongo-specific, notification
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }

        return list;
    }

    /// Returns all docs which contain the specified key:value in an array
    public ArrayList<Document> retrieveByKeyValue(String key, String value){
        /// Same vars
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";
        ArrayList<Document> list = new ArrayList<>();

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(db_name);
            MongoCollection<Document> collection = db.getCollection(collection_name);

            /// Retrieval of data - actual use of funct
            FindIterable<Document> content = collection.find(Filters.eq(key, value));
            for (Document doc : content) {
                list.add(doc);
            }

            /// DESTROY CONNECTION
            mongoClient.close();
        }

        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }

        return list;
    }

    /// Identifies a doc with the value of the id-key, adds a new key:value at end
    /// OR overwrites existing value if key already exists
    //TODO:
    public void appendData(String idValue, String addKey, Object addValue) {
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(db_name);
            MongoCollection<Document> collection = db.getCollection(collection_name);

            /// search by and insertion of param - actual use of funct
            collection.updateOne(Filters.eq("id", idValue), Updates.set(addKey, addValue));

            /// DESTROY CONNECTION
            mongoClient.close();
        }

        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }

    /// Removes key and value in specified doc at specified key
    //TODO: What if specified key does not exist? (It does nothing), make error message?
    public void removeData(String idValue, String removeKey) {
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(db_name);
            MongoCollection<Document> collection = db.getCollection(collection_name);

            /// remove key and value at specified key - actual use of funct
            collection.updateOne(Filters.eq("id", idValue), Updates.unset(removeKey));

            /// DESTROY CONNECTION
            mongoClient.close();
        }

        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }

    /// Deletes the first document with a specified ID
    public void deleteOneDocument(String idValue) {
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(collection_name);

        /// remove key and value at specified key - actual use of funct
        collection.deleteOne(Filters.eq("id", idValue));

        /// DESTROY CONNECTION
        mongoClient.close();
    }

    /// Deletes all documents with a specified ID (if duplicates exist)
    public void deleteManyDocuments(String idValue) {
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(collection_name);

        /// remove key and value at specified key - actual use of funct
        collection.deleteMany(Filters.eq("id", idValue));

        /// DESTROY CONNECTION
        mongoClient.close();
    }

    /// Searches the entire collection for a term and adds the containing doc to the return array
    // if alot of data this will take alot of processing time
    // not tested, will likely have issues with nested dictionaries but work with direct values
    public ArrayList<Document> retrieveByValue(String searchTerm) {
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "testdata";
        ArrayList<Document> list = new ArrayList<>();

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(collection_name);

        /// Retrieval of data - actual use of funct
        MongoCursor<Document> cursor = collection.find().iterator(); //find() henter alt uten param
        //iterator() sørger for en returtype som kan behandles av MongoCursor (som behandler data mer effektivt enn å lese inn absolutt alt selv)
        while (cursor.hasNext()) { //for each item the mongocursor holds
            Document doc = cursor.next(); //associating the cursor item with a datatype and var
            for (String key : doc.keySet()) {
                if (doc.get(key).equals(searchTerm) || key.equals(searchTerm)) { //if value or key of doc matches input-value
                    list.add(doc); //save for later
                }
            }
            //if list.isEmpty() then create error message ..
        }

        /// DESTROY CONNECTION
        mongoClient.close();

        return list;

    }
}
