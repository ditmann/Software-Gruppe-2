package no.avandra.classes;

import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

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
    /// 2. Gjøre variablene globale, private, get'ers, set'ers


    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// VARIABLES
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    private String user = "siljemst_db_user";
    private String pass = "Avandra1234567890";
    private String db_name = "dummy";
    private String collection_name = "testdata";
    private ArrayList<Document> list = new ArrayList<>();
    private String idField = "id";

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// GET'ERS & SET'ERS
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
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

    public ArrayList<Document> getList() {
        return list;
    }

    public String getIdField() {
        return idField;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setList(ArrayList<Document> list) {
        this.list = list;
    }

    public void setCollectionName(String collection_name) {
        this.collection_name = collection_name;
    }

    public void setDbName(String db_name) {
        this.db_name = db_name;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setIdField(String idField) {this.idField = idField;}

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// METHODS
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|


    /// Creates a doc with the given content at the specified db and collection
    //TODO: prevent duplicates:
    //make it use appendData if id already exists? maybe just not work?
    public void createUser(String key, Object object){
        /// for future use: take input?
        // find secure way to assign variables from front end (?) or store securely closer to core(?)
        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(getDbName());
            MongoCollection<Document> collection = db.getCollection(getCollectionName());

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

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(getDbName());
            MongoCollection<Document> collection = db.getCollection(getCollectionName());

            /// Retrieval of data - actual use of funct
            FindIterable<Document> content = collection.find();
            for (Document doc : content) {
                getList().add(doc);
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

        return getList();
    }

    /// Returns all docs which contain the specified key:value in an array
    public ArrayList<Document> retrieveByKeyValue(String key, String value){

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(getDbName());
            MongoCollection<Document> collection = db.getCollection(getCollectionName());

            /// Retrieval of data - actual use of funct
            FindIterable<Document> content = collection.find(Filters.eq(key, value));
            for (Document doc : content) {
                getList().add(doc);
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

        return getList();
    }

    /// Identifies a doc with the value of the id-key, adds a new key:value at end
    /// OR overwrites existing value if key already exists
    //TODO:
    public void appendData(String idValue, String addKey, Object addValue) {

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(getDbName());
            MongoCollection<Document> collection = db.getCollection(getCollectionName());

            /// search by and insertion of param - actual use of funct
            collection.updateOne(Filters.eq(getIdField(), idValue), Updates.set(addKey, addValue));

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

        try {
            /// INITIALIZE CONNECTION
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getUser() + "@avandra.pix7etx.mongodb.net/" + "db");

            /// which db in the client, which collection in the db
            MongoDatabase db = mongoClient.getDatabase(getDbName());
            MongoCollection<Document> collection = db.getCollection(getCollectionName());

            /// remove key and value at specified key - actual use of funct
            collection.updateOne(Filters.eq(getIdField(), idValue), Updates.unset(removeKey));

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

    /// Deletes the first document with a specified ID //start here
    public void deleteOneDocument(String idValue) {

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(getDbName());
        MongoCollection<Document> collection = db.getCollection(getCollectionName());

        /// remove key and value at specified key - actual use of funct
        collection.deleteOne(Filters.eq(getIdField(), idValue));

        /// DESTROY CONNECTION
        mongoClient.close();
    }

    /// Deletes all documents with a specified ID (if duplicates exist)
    public void deleteManyDocuments(String idValue) {

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(getDbName());
        MongoCollection<Document> collection = db.getCollection(getCollectionName());

        /// remove key and value at specified key - actual use of funct
        collection.deleteMany(Filters.eq(getIdField(), idValue));

        /// DESTROY CONNECTION
        mongoClient.close();
    }

    /// Searches the entire collection for a term and adds the containing doc to the return array
    // if alot of data this will take alot of processing time
    // not tested, will likely have issues with nested dictionaries but work with direct values
    public ArrayList<Document> retrieveByValue(String searchTerm) {

        /// INITIALIZE CONNECTION
        MongoClient mongoClient = MongoClients.create("mongodb+srv://" + getUser() + ":" + getPass() + "@avandra.pix7etx.mongodb.net/" + "db");

        /// which db in the client, which collection in the db
        MongoDatabase db = mongoClient.getDatabase(getDbName());
        MongoCollection<Document> collection = db.getCollection(getCollectionName());

        /// Retrieval of data - actual use of funct
        MongoCursor<Document> cursor = collection.find().iterator(); //find() henter alt uten param
        //iterator() sørger for en returtype som kan behandles av MongoCursor (som behandler data mer effektivt enn å lese inn absolutt alt selv)
        while (cursor.hasNext()) { //for each item the mongocursor holds
            Document doc = cursor.next(); //associating the cursor item with a datatype and var
            for (String key : doc.keySet()) {
                if (doc.get(key).equals(searchTerm) || key.equals(searchTerm)) { //if value or key of doc matches input-value
                    getList().add(doc); //save for later
                }
            }
            //if list.isEmpty() then create error message ..
        }

        /// DESTROY CONNECTION
        mongoClient.close();

        return getList();

    }


}
