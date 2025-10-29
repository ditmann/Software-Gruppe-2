package avandra.storage.adapter;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import avandra.core.domain.Coordinate;
import avandra.core.port.DBConnection;
import avandra.core.port.DBHandler;

public class MongoDBHandler implements DBHandler {

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// VARIABLE(S)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    private ArrayList<Document> list = new ArrayList<>();
    private String idField = "id";

    // NOT given get'ers & set'ers as this application only accesses this specific db and collection
    private DBConnection mongoDBConnection;
    private MongoCollection<Document> collection;

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// CONSTRUCTOR(S)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    public MongoDBHandler(DBConnection connection) {
        this.mongoDBConnection = connection;
    }

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// GET'ER(s) & SET'ER(s)
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
    /// METHOD(S)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    /// Creates a new document in the collection which represents a user, with all required fields
    public void createUser(String userID, boolean adminUser){

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {

            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: creating and inserting the keys (and values where required), if the ID doesn't exist
            Boolean existingID = collection.find(Filters.eq(getIdField(), userID)).iterator().hasNext();
            if (existingID) {
                System.out.println("Denne ID'en eksisterer allerede");
            }
            else {
                Document userDoc = new Document("id", userID)
                        .append("admin", adminUser);

                List<String> litebrukere = new ArrayList<>();
                Document planned_trips = new Document();
                Document favorites = new Document();
                userDoc.append("litebrukere", litebrukere).append("planlagte reiser", planned_trips).append("favoritter", favorites);

                collection.insertOne(userDoc);
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }


    public void addDestinationToFavorites(String userID, String destinationName, String address, double latitude, double longitude) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: creates all necessary data from input, assembles hierarchy and adds to favoritter
            Document coordinates = new Document("latitude", latitude).append("longitude", longitude);
            Document destinationDetails = new Document("adresse", address).append("koordinater", coordinates);
            Document update = new Document("$set", new Document("favoritter." + destinationName, destinationDetails));

            collection.updateOne(Filters.eq("id", userID), update);
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }


    //TODO: should be generalized to add coordinates to any list? Now just to favorites
    // OR: change name of method
    public void addCoordinatesToDestination(String userID, String destinationName, double latitude, double longitude) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: Adds coordinates to the "koordinater"-key under an input-key for destination
            /// in the list "favoritter"
            Document coordinates = new Document("latitude", latitude).append("longitude", longitude);
            Document update = new Document("$set", new Document("favoritter." + destinationName + ".koordinater", coordinates));

            collection.updateOne(Filters.eq("id", userID), update);
        }
        /// Super basic error "handling" + specified if Mongo-error
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

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: Find and retrieve data
            FindIterable<Document> content = collection.find();
            for (Document doc : content) {
                getList().add(doc);
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
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


    /// TODO: add description of what it does
    public Coordinate searchDestination(String userID, String destinationID){
        String coordinateFieldName = "koordinater";

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: find and retrieve data where there is
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
        /// Super basic error "handling" + specified if Mongo-error
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


    /// TODO: WHAT DOES THIS DO? returns null no matter what ?
    @Override
    public Coordinate destinationCoordinate(String name) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic:
            Document userDoc = collection.find(Filters.eq("id", name)).first();
            if (userDoc == null) return null;
        }
        /// Super basic error "handling" + specified if Mongo-error
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

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: Retrieve data
            FindIterable<Document> content = collection.find(Filters.eq(key, value));
            if (content.iterator().hasNext()) {
                for (Document doc : content) {
                    getList().add(doc);
                }
            }
            else {
                System.out.println("No match for key and value.");
                //tom liste vil returneres
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
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

    @Override
    public void createUser(String userID, boolean adminUser, String favoriteDestination, String address, double latitude, double longitude) {

    }

    /// Identifies a doc with the value of the id-key, adds a new key:value at end
    /// OR overwrites existing value if key already exists
    public void appendData(String userID, String addKey, Object addValue) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: search by and insertion of parameters
            if (collection.countDocuments(Filters.eq(getIdField(), userID)) > 0) {
                collection.updateOne(Filters.eq(getIdField(), userID), Updates.set(addKey, addValue));
            }
            else {
                System.out.println("Found no match for " + userID + "\n");
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }

    /// Removes key and value in specified doc at specified key, if it exists
    public void removeData(String userID, String keyToRemove) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: remove key and value at specified key
            Boolean existingID = collection.find(Filters.eq(getIdField(), userID)).iterator().hasNext();
            if (existingID) {
                collection.updateOne(Filters.eq(getIdField(), userID), Updates.unset(keyToRemove));
            }
            else {
                System.out.println("\nThe ID \"userID\" does not exist.");
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }

    //deletes a key for a user, (userid, the key to remove, the type of destination[path])
    public void removeData(String userID, String keyToRemove, String destinationType) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: remove key and value at specified key
            Boolean existingID = collection.find(Filters.eq(getIdField(), userID)).iterator().hasNext();
            if (existingID) {
                collection.updateOne(Filters.eq(getIdField(), userID),
                Updates.unset(destinationType + "." + keyToRemove));
            }
            else {
                System.out.println("\nThe ID \"userID\" does not exist.");
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }

    }

    //deletes a key for a user, (userid, the key to remove, the type of destination[path], the specific destination[path])
    public void removeData(String userID, String keyToRemove, String destinationType, String destinationKey) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: remove key and value at specified key
            Boolean existingID = collection.find(Filters.eq(getIdField(), userID)).iterator().hasNext();
            if (existingID) {
                collection.updateOne(Filters.eq(getIdField(), userID),
                Updates.unset(destinationType + "." + destinationKey + "." + keyToRemove));
            }
            else {
                System.out.println("\nThe ID \"userID\" does not exist.");
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
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
    public void removeData(String userID) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: remove key and value at specified key
            if (collection.countDocuments(Filters.eq(getIdField(), userID)) > 0) {
                collection.deleteOne(Filters.eq(getIdField(), userID));
            }
            else {
                System.out.println("Found no match for " + userID + "\n");
            }
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }

    /// Oppdater/legg til destinasjon hos lite-bruker, KUN hvis adminId har tilgang i admins.allowedLiteUsers
    /// TODO: what if something doesnt exist, error message?
    public boolean insertDestinationForLiteUser(
            String liteUserId,
            String destId,
            String name,
            String address,
            Double lat,
            Double lng,
            String adminId
    ) {

        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic:
            //Check that admin has access to this lite-user
            Document admin = collection.find(
                    Filters.and(
                            Filters.eq("id", adminId),
                            Filters.in("allowedLiteUsers", liteUserId)
                    )
            ).first();

            //attempt to UPDATE existing destination
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

            //If did not exist: PUSH new destination
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
        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
            return false;
        }
    }


    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    ///  METHODS NOT FROM INTERFACE
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    /// Deletes all documents with a specified ID
    // necessary for developers in case of duplicate ID entries
    public void deleteManyDocuments(String userID) {
        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: remove key and value at specified key
            if (collection.countDocuments(Filters.eq(getIdField(), userID)) > 0) {
                collection.deleteMany(Filters.eq(getIdField(), userID));
            }
            else {
                System.out.println("Found no match for " + userID + "\n");
            }

        }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
        }
    }

    /// Searches the entire collection for a term and adds the containing doc to the return array
    // if alot of data this will take alot of processing time
    // useful for developers in testing
    public ArrayList<Document> retrieveByValue(String searchTerm) {
        try (MongoDBConnection connection = (MongoDBConnection) mongoDBConnection.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: retrieval of data
            MongoCursor<Document> cursor = collection.find().iterator(); //find() fetches everything
            //iterator() ensures a return type MongoCursor can use, for efficient data handling
             while (cursor.hasNext()) { //for each item the mongocursor holds
                Document doc = cursor.next(); //associating the cursor item with a datatype and variable
                for (String key : doc.keySet()) {
                    if (doc.get(key).equals(searchTerm) || key.equals(searchTerm)) { //if value or key of doc matches input-value
                        getList().add(doc); //save for later
                }
            }
        }
    }
        /// Super basic error "handling" + specified if Mongo-error
        catch (MongoException e) {
        System.out.println("\nMongoDB exception: ");
        e.printStackTrace();
    }
        catch (Exception e) {
        System.out.println("\nNon-DB exception: ");
        e.printStackTrace();
    }

        if (getList().isEmpty()) {
            System.out.println("Found no match for " + searchTerm + "\n");
        }
        //returns an empty list if search term had no matches
        return getList();
    }
}