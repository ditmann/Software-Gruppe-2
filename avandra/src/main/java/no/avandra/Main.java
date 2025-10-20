
package no.avandra;

import com.mongodb.client.FindIterable;
import no.avandra.classes.Coordinate;
import no.avandra.classes.Destinasjon;
import no.avandra.classes.*;
import org.bson.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Dictionary;

public class Main {
    public static void main(String[] args) throws Exception {

        //lag key og value'
        String id = "id";
        String name = "Kåre";
        String age = "28";
        String docKey = "Hjem";
        String destinationName = "Hjem";
        String destinationAdress = "Dr. Ellertsens Vei";
        Coordinate coords = 59.4433, 11.0008;


        //legg til i DOC
        Document doc = new Document();
        Document destination1 = new Document();
        destination1.append(docKey, destinationName);
        destination1.append("Adresse", destinationAdress);
        destination1.append("Koordinater", "1");
        doc.append("Hjem", destination1);
        ArrayList<Document> favoritter = new ArrayList<>();
        Document destinasjon = new Document();
        destinasjon.append("Hjem", destination1);
        System.out.println(destination1);
        System.out.println(destinasjon);
        favoritter.add(destinasjon);
        System.out.println(favoritter);


        //legg så DOC til i LISTE
        //Bruker kaare = new AdminBruker(name);

        MongoDBHandler dbHandler = new MongoDBHandler();
        System.out.println(dbHandler.SearchDestination("Timmy", "favorites", "Hjem"));



        //dbHandler.appendData("Kåre", "Favoritter", favoritter);

        /*
        Coordinate c = dbHandler.destinationCoordinate("Timmy");

        if (c != null) {
            System.out.println("Kordinater: " );
        }
        else {
            System.out.println("Fant ikke koordinater.");
    }

         */









        //dbHandler.appendData(id, "navn", name);
        //dbHandler.appendData(name, "age", age);

        //dbHandler.sendData(id, "Kåre");
        //dbHandler.appendData(name, "age", age);
        //dbHandler.appendData(docKey, "Favoritter", favoritter);


        /*ArrayList<Document> data;
        data = dbHandler.retrieveAllData();
        for (Document doc : data) {
            System.out.println(doc);
        }*/
    }
}
            /*
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
        /// VARIABLES
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
     MongoDBHandler mongodbhandler = new MongoDBHandler();
     Object x = mongodbhandler.retrieveCoordinates("Timmy", "favorites", "Hjem");
        System.out.println(x);

        /*
       //you can sequentially create then add
        //if duplicates exist will add to first and no others
        String string = "id"; //Key'en vi søker etter
        String name = "Hjordis"; //Value'en vi søker med //som blir satt inn
        String age = "78";
        //String docKey = "Turn";
        String destinationName = "Kaféen til Turid";
        String destinationAdress = "Hjerteveien 4";
        String destinationCoords = "9876";

        //list of locations / information related to each other
        /// In Avandra: favorite destinations
        Document listDoc = new Document();
        //list of information about that particular thing
        /// In Avandra: name, coords etc about location
        Document destination = new Document();
        //adding the info about the specific thing
        destination.append(destinationName, destinationName);
        destination.append("Adresse", destinationAdress);
        destination.append("Koordinater", destinationCoords);

        Document newDestination = new Document();
        newDestination.append("Hjem", "Hjem");
        newDestination.append("Adresse", "Min leilighet 15");
        newDestination.append("Koordinater", "7654");

        Document anotherDestination = new Document();
        anotherDestination.append("Frisor", "Cutters");
        anotherDestination.append("Adresse", "Sakseveien 169");
        anotherDestination.append("Koordinater", "5432");


        Bruker bruker = new AdminBruker(name);
        ArrayList<Document> listOfDoc = new ArrayList<>();

        /// ------------------------------------------------------
        MongoDBHandler mongodbhandler = new MongoDBHandler();

        //mongodbhandler.createUser(string, name);
        mongodbhandler.appendData(string, "age", age);

        //mongodbhandler.deleteOneDocument("Hjordis");

        listDoc.append(destination); //dette gjøres ved hver kjøring og overskriver dermed listen
        listDoc.append(newDestination);
        listDoc.append(anotherDestination);
        System.out.println(listDoc);

        //mongodbhandler.appendData(name, "favorites", listDoc);

        /*
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
        /// THE DOING OF THE THING
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----

        MongoDBHandler mongodbhandler = new MongoDBHandler();

        //mongodbhandler.deleteOneDocument(name);
        //mongodbhandler.removeData(name, "Ajahs");
        ///first field: "id":name
       mongodbhandler.sendData(string, bruker);
        ///second field: search by "id"-value, "age": age
       mongodbhandler.appendData(name, "age", age);



        ///third field: list of dicts: "dict": dict
        // create a dict
        // add entries to doc
        // add entries to wrapping doc
        /// Is this better solved with a hashmap (NO IT IS NOT DONT DO IT)
        listDoc.append(docKey, destination); //dette gjøres ved hver kjøring og overskriver dermed listen
        listDoc.append("Hjem", newDestination);
        listDoc.append("Pappa", anotherDestination);

        mongodbhandler.appendData(name, "favorites", listDoc);


<<<<<<< HEAD
 */
            /*
=======



>>>>>>> feature/methodForAdministratingLite
        ArrayList<Document> data;
        data = mongodbhandler.retrieveAllData();
        for (Document doc : data) {
            System.out.println(doc);
        }


  String id = "id";
  String name = "Kåre";
  String age = "28";
  String docKey = "Kåre";
  String destinationName = "ELSKE SKOLEN";
  String destinationAdress = "BESTE STEDET";
  Coordinate coords = new Coordinate((float)60.00000, (float)10.00000);
/*
  //adding
    Document liste = new Document();
    Document destination1 = new Document();
    destination1.append("Hjem", destinationName);
    System.out.println(destination1);
    destination1.append("adresse", destinationAdress);
        System.out.println(destination1);
    destination1.append("koordinater", "KOORDINATER");
        System.out.println(destination1);
    ArrayList<Document> favoritter = new ArrayList<>();
    Document favoritt = new Document();
    favoritt.append("Hjem", destination1);
        System.out.println(destination1);
    favoritter.add(favoritt);
    System.out.println(favoritter);

    MongoDBHandler handler = new MongoDBHandler();
    handler.appendData("Kåre", "Favoritter", favoritter);
    //handler.removeData("Kåre", "Favoritter");



/// Update a destination only if adminUserId is in approvedAdmins.
    /// Returns true if updated or inserted; false otherwise.
    public boolean insertDestinationForUser(
            String targetUserId,
            String destId,
            String name,
            String address,
            Double lat,
            Double lng,
            String adminUserId
    ) {
        String user = "siljemst_db_user";
        String pass = "Avandra1234567890";
        String db_name = "dummy";
        String collection_name = "users"; // change if needed

        try {
            MongoClient mongoClient = MongoClients.create("mongodb+srv://" + user + ":" + pass + "@avandra.pix7etx.mongodb.net/" + "db");
            MongoDatabase db = mongoClient.getDatabase(db_name);
            MongoCollection<Document> collection = db.getCollection(collection_name);

            // Try UPDATE existing destination, only if caller is approved
            com.mongodb.client.result.UpdateResult updateRes = collection.updateOne(
                    Filters.and(
                            Filters.eq("id", targetUserId),
                            Filters.in("approvedAdmins", adminUserId),
                            Filters.eq("favorites.destId", destId)
                    ),
                    Updates.combine(
                            Updates.set("favorites.$.name", name),
                            Updates.set("favorites.$.address", address),
                            Updates.set("favorites.$.coords",
                                    new Document()
                                            .append("lat", lat)
                                            .append("lng", lng) ) ));
            if (updateRes.getModifiedCount() == 1) {
                mongoClient.close();
                return true;
            }

            // If not present, INSERT new destination only if caller is approved
            Document coords = new Document();
            if (lat != null) coords.put("lat", lat);
            if (lng != null) coords.put("lng", lng);

            Document destDoc = new Document("destId", destId)
                    .append("name", name)
                    .append("address", address)
                    .append("coords", coords);
            com.mongodb.client.result.UpdateResult insertRes = collection.updateOne(
                    Filters.and(
                            Filters.eq("id", targetUserId),
                            Filters.in("approvedAdmins", adminUserId),
                            Filters.ne("favorites.destId", destId)
                    ),
                    Updates.push("favorites", destDoc)
            );

            mongoClient.close();
            return insertRes.getModifiedCount() == 1;
        }
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

    */

    }
}
*/

