package avandra.storage.adapter;

import java.util.ArrayList;
import java.util.List;

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

import avandra.core.domain.Coordinate;
import avandra.core.port.DBHandler;

import javax.print.Doc;

public class MongoDBHandler implements DBHandler {

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// VARIABLES
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    private ArrayList<Document> list = new ArrayList<>();
    private String idField = "id";
    private MongoDBConnection mongoDBConnection;

    public MongoDBHandler(MongoDBConnection connection) {
        this.mongoDBConnection = connection;
    }

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// GET'ERS & SET'ERS
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    public ArrayList<Document> getList() {
        return list;
    }

    public String getIdField() {
        return idField;
    }

    public void setList(ArrayList<Document> list) {
        this.list = list;
    }

    public void setIdField(String idField) {this.idField = idField;}

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// METHODS
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|


    /// Creates a doc with the given content at the specified db and collection
    //TODO: prevent duplicates:
    //make it use appendData if id already exists? maybe just not work?

    /// Oppretter en bruker med tomme dokumenter/lister
    public void createUser(String userID, boolean adminUser){
        /// for future use: take input?
        // find secure way to assign variables from front end (?) or store securely closer to core(?)
        try  {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            /// insertion of param - actual use of funct
            Document userDoc = new Document("id", userID)
                    .append("admin", adminUser);

            List<String> litebrukere = new ArrayList<>();
            Document planned_trips = new Document();
            Document favorites = new Document();

            userDoc.append("litebrukere", litebrukere).append("planlagte reiser", planned_trips).append("favoritter", favorites);

            collection.insertOne(userDoc);

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

    /// Funksjon for å legge til destinasjoner til favoritter
    public void addDestinationToFavorites(String userID, String destinationName, String address, double latitude, double longitude) {

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            Document coordinates = new Document("latitude", latitude).append("longitude", longitude);
            Document destinationDetails = new Document("adresse", address).append("koordinater", coordinates);

            Document update = new Document("$set", new Document("favoritter." + destinationName, destinationDetails));

            collection.updateOne(Filters.eq("id", userID), update);

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


    public void addCoordinatesToDestination(String userID, String destinationName, double latitude, double longitude) {

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            Document coordinates = new Document("latitude", latitude).append("longitude", longitude);

            Document update = new Document("$set", new Document("favoritter." + destinationName + ".koordinater", coordinates));

            collection.updateOne(Filters.eq("id", userID), update);
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


    /// Returns all documents in the collection as an array
    public ArrayList<Document> retrieveAllData() {

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            /// Retrieval of data - actual use of funct
            FindIterable<Document> content = collection.find();
            for (Document doc : content) {
                getList().add(doc);
            }
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


    public Coordinate searchDestination(String userID, String destinationID){

        /// Same vars
        String coordinateFieldName = "koordinater";

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            /// Retrieval of data - actual use of funct
            Document userDoc = collection.find(Filters.eq("id", userID)).first();
            if (userDoc == null) return null;

            Document destinationTypeDoc = (Document)
                    userDoc.get("favoritter");
            if (destinationTypeDoc == null) return null;

            Document destinationDoc = (Document)
                    destinationTypeDoc.get(destinationID);
            if (destinationDoc == null) return null;

            Document coordinates = (Document)
                    destinationDoc.get(coordinateFieldName);
            if (coordinates == null) return null;

            double lat = coordinates.getDouble("latitude");
            double lon = coordinates.getDouble("longitude");

            return new Coordinate(lat, lon);
        }
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Coordinate destinationCoordinate(String name) {

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            ///  Må finne bruker
            Document userDoc = collection.find(Filters.eq("id", name)).first();
            if (userDoc == null) return null;
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
        return null;
    }


    /// Returns all docs which contain the specified key:value in an array
    public ArrayList<Document> retrieveByKeyValue(String key, String value){

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            /// Retrieval of data - actual use of funct
            FindIterable<Document> content = collection.find(Filters.eq(key, value));
            for (Document doc : content) {
                getList().add(doc);
            }
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
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            /// search by and insertion of param - actual use of funct
            collection.updateOne(Filters.eq(getIdField(), idValue), Updates.set(addKey, addValue));
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
    public void removeData(String userID, String removeKey) {

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            /// remove key and value at specified key - actual use of funct
            collection.updateOne(Filters.eq(getIdField(), userID), Updates.unset(removeKey));
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

    //TODO: make:
    public void removeData(String userID, String removeKey, String destinationType) {}
    public void removeData(String userID, String keyToRemove, String destinationType, String destinationKey) {}


    /// Deletes the first document with a specified ID //start here
    public void removeData(String userID) {
        /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
        MongoCollection<Document> collection = mongoDBConnection.getCollection();

        /// remove key and value at specified key - actual use of funct
        collection.deleteOne(Filters.eq(getIdField(), userID));
    }

    
    




/*
    public Coordinate destinationCoordinate(){

        return new Coordinate(destinationCoordinate().getLatitudeNum(), destinationCoordinate().getLongitudeNUM());
    } */

    /// Oppdater/legg til destinasjon hos lite-bruker, KUN hvis adminId har tilgang i admins.allowedLiteUsers
    public boolean insertDestinationForLiteUser(
            String liteUserId,
            String destId,
            String name,
            String address,
            Double lat,
            Double lng,
            String adminId
    ) {

        try {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            MongoCollection<Document> collection = mongoDBConnection.getCollection();

            //  Sjekk at admin har tilgang til denne lite-brukeren
            Document admin = collection.find(
                    Filters.and(
                            Filters.eq("id", adminId),
                            Filters.in("allowedLiteUsers", liteUserId)
                    )
            ).first();

            // Forsøk å OPPDATERE eksisterende destinasjon
            var updateRes = collection.updateOne(
                    Filters.and(
                            Filters.eq("id", liteUserId),
                            Filters.eq("favorites.destId", destId)
                    ),
                    Updates.combine(
                            Updates.set("favorites.$.name", name),
                            Updates.set("favorites.$.address", address),
                            Updates.set("favorites.$.coords", new Document()
                                    .append("lat", lat)
                                    .append("lng", lng)
                            ),
                            Updates.set("favorites.$.updatedAt", java.time.Instant.now()),
                            Updates.set("favorites.$.adminId", adminId)));

            // Hvis ikke fantes: PUSH ny destinasjon
            Document coords = new Document();
            if (lat != null) coords.put("lat", lat);
            if (lng != null) coords.put("lng", lng);

            Document destDoc = new Document("destId", destId)
                    .append("name", name)
                    .append("address", address)
                    .append("coords", coords)
                    .append("adminId", adminId)
                    .append("addedAt", java.time.Instant.now());

            var insertRes = collection.updateOne(
                    Filters.and(
                            Filters.eq("id", liteUserId),
                            Filters.ne("favorites.destId", destId) // ingen eksisterende dest med samme id
                    ),
                    Updates.push("favorites", destDoc)
            );
            return insertRes.getModifiedCount() == 1;

        } catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
            return false;
        }
    }


    /*
     * ----------------------------------------------------------------------------------------------------------------------
     * ting som ikke er i interface
     * --------------------------------------------------------------------------------------------------------------------------
     */
    /// Deletes all documents with a specified ID (if duplicates exist)
    public void deleteManyDocuments(String idValue) {
        /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
        MongoCollection<Document> collection = mongoDBConnection.getCollection();

        /// remove key and value at specified key - actual use of funct
        collection.deleteMany(Filters.eq(getIdField(), idValue));
    }


    /// Searches the entire collection for a term and adds the containing doc to the return array
    // if alot of data this will take alot of processing time
    // not tested, will likely have issues with nested dictionaries but work with direct values
    public ArrayList<Document> retrieveByValue(String searchTerm) {
        /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
        MongoCollection<Document> collection = mongoDBConnection.getCollection();

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
        return getList();
    }
}