/**
 * Brukerene våres lever bare i databasen
 *
 *
 *
 */

package avandra.storage.adapter;

import java.util.ArrayList;
import java.util.List;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.DBHandlerPort;
import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import avandra.core.port.DBConnectionPort;

public class MongoDBHandlerAdapter implements DBHandlerPort {

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// VARIABLE(S)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    private String idField = "id";

    // NOT given get'ers & set'ers as this application only accesses this specific db and collection
    private DBConnectionPort mongoDBConnectionPort;
    private MongoCollection<Document> collection;

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// CONSTRUCTOR(S)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    public MongoDBHandlerAdapter(DBConnectionPort connection) {
        this.mongoDBConnectionPort = connection;
    }

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// GET'ER(s) & SET'ER(s)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|



    public String getIdField() {
        return idField;
    }



    public void setIdField(String idField) {this.idField = idField;}

    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /// METHOD(S)
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    /**
     * Creates a new document in the collection which represents a user, with all required fields
     * @param userID
     * @param adminUser
     */
    public void createUser(String userID, boolean adminUser){
        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {

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

                List<String> liteUserList = new ArrayList<>();
                Document plannedTrips = new Document();
                Document favorites = new Document();
                userDoc.append("litebrukere", liteUserList).append("planlagte reiser", plannedTrips).append("favoritter", favorites);

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

    /**
     *
     * @param userID
     * @param destinationName
     * @param address
     * @param latitude
     * @param longitude
     */
    public void addDestinationToFavorites(String userID, String destinationName, String address, double latitude, double longitude) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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


    /**
     *
     * @param userID
     * @param destinationName
     * @param latitude
     * @param longitude
     */
    public void addCoordinatesToFavDestination(String userID, String destinationName, double latitude, double longitude) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     * @return Returns all documents in the collection as an array
     */
    public ArrayList<Document> retrieveAllData() {
        ArrayList<Document> out = new ArrayList<>();

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: Find and retrieve data
            FindIterable<Document> content = collection.find();
            for (Document doc : content) {
                out.add(doc);
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

        return out;
    }


    /**
     *
     * @param userID
     * @param destinationID
     * @return
     */
    public CoordinateDTO searchFavDestination(String userID, String destinationID){
        String coordinateFieldName = "koordinater";

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

            return new CoordinateDTO(lat, lon);
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

    //TODO: make
    @Override
    public void createUser(String userID, boolean adminUser, String favoriteDestination, String address, double latitude, double longitude) {

    }

    /**
     *
     * @param key
     * @param value
     * @return Returns all docs which contain the specified key:value in an array
     */
    public ArrayList<Document> retrieveByKeyValue(String key, String value){
        ArrayList<Document> out = new ArrayList<>();
        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();
            /// Method Logic: Retrieve data
            FindIterable<Document> content = collection.find(Filters.eq(key, value));
            if (content.iterator().hasNext()) {
                for (Document doc : content) {
                    out.add(doc);
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

        return out;
    }


    /**
     * Identifies a doc with the value of the id-key, adds a new key:value at end
     * OR overwrites existing value if key already exists
     * @param userID
     * @param addKey
     * @param addValue is Object to allow appending Documents, ArrayLists and Strings
     */
    public void appendData(String userID, String addKey, Object addValue) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     * Removes key and value in specified doc at specified key, if it exists
     * @param userID
     * @param keyToRemove
     */
    public void removeData(String userID, String keyToRemove) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     * deletes a key for a user, (userid, the key to remove, the type of destination[path])
     * @param userID
     * @param keyToRemove
     * @param destinationType
     */
    public void removeData(String userID, String keyToRemove, String destinationType) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     * deletes a key for a user, (userid, the key to remove, the type of destination[path], the specific destination[path])
     * @param userID
     * @param keyToRemove
     * @param destinationType
     * @param destinationKey
     */
    public void removeData(String userID, String keyToRemove, String destinationType, String destinationKey) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     * Deletes the first document with a specified ID //start here
     * @param userID
     */
    public void removeData(String userID) {

        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> listUserFavDestinations(String userId) {
        java.util.List<String> out = new java.util.ArrayList<>();

        try (MongoDBConnectionAdapter connection =
                     (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
            collection = connection.getCollection();

            Document userDoc = collection.find(com.mongodb.client.model.Filters.eq("id", userId)).first();
            if (userDoc == null) return out;

            Document favDoc = userDoc.get("favoritter", Document.class);
            if (favDoc == null) return out;

            // Just collect the keys (names) under "favoritter"
            for (String name : favDoc.keySet()) {
                if (name != null && !name.isBlank()) out.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    /**
     *
     * @param adminId
     * @return
     */
    @Override
    public List<String> listLitebrukereForAdmin(String adminId) {
        List<String> out = new ArrayList<>();
        try (MongoDBConnectionAdapter connection =
                     (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {

            collection = connection.getCollection();
            Document adminDoc = collection.find(com.mongodb.client.model.Filters.eq("id", adminId)).first();
            if (adminDoc == null) return out;

            Object lite = adminDoc.get("litebrukere"); // can be String or List
            if (lite instanceof String s) {
                if (!s.isBlank()) out.add(s);
            } else if (lite instanceof java.util.List<?> list) {
                for (Object o : list) {
                    if (o instanceof String s && !s.isBlank()) out.add(s);
                }
            }
            return out;

        } catch (com.mongodb.MongoException e) {
            System.out.println("\nMongoDB exception: ");
            e.printStackTrace();
            return out;
        } catch (Exception e) {
            System.out.println("\nNon-DB exception: ");
            e.printStackTrace();
            return out;
        }
    }

    /**
     *
     * @param userId
     * @return
     */
    @Override
    public boolean isAdmin(String userId) {
        try (MongoDBConnectionAdapter connection =
                     (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
            collection = connection.getCollection();

            Document doc = collection.find(Filters.eq("id", userId)).first();
            if (doc == null) return false;

            Boolean admin = doc.getBoolean("admin", false);
            return admin != null && admin;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|
    ///  METHODS NOT FROM INTERFACE
    ///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|

    /**
     * Deletes all documents with a specified ID
     * necessary for developers in case of duplicate ID entries
     * @param userID
     */
    public void deleteManyDocuments(String userID) {
        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {
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

    /**
     * Searches the entire collection for a term and adds the containing doc to the return array
     * if alot of data this will take alot of processing time
     * useful for developers in testing
     * @param searchTerm
     * @return
     */
    public ArrayList<Document> retrieveByValue(String searchTerm) {
        ArrayList<Document> out = new ArrayList<>();
        try (MongoDBConnectionAdapter connection = (MongoDBConnectionAdapter) mongoDBConnectionPort.open()) {

            /// Opens AutoCloseable connection to db and returns a specific collection defined in the class
            collection = connection.getCollection();

            /// Method Logic: retrieval of data
            MongoCursor<Document> cursor = collection.find().iterator(); //find() fetches everything
            //iterator() ensures a return type MongoCursor can use, for efficient data handling
             while (cursor.hasNext()) { //for each item the mongocursor holds
                Document doc = cursor.next(); //associating the cursor item with a datatype and variable
                for (String key : doc.keySet()) {
                    if (doc.get(key).equals(searchTerm) || key.equals(searchTerm)) { //if value or key of doc matches input-value
                        out.add(doc); //save for later
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

        if (out.isEmpty()) {
            System.out.println("Found no match for " + searchTerm + "\n");
        }
        //returns an empty list if search term had no matches
        return out;
    }






}