package no.avandra;

import com.mongodb.client.FindIterable;
import no.avandra.classes.Coordinate;
import no.avandra.classes.Destinasjon;
import no.avandra.classes.*;
import org.bson.Document;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
        /// VARIABLES
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
        //you can sequentially create then add
        //if duplicates exist will add to first and no others
        String string = "id"; //Key'en vi søker etter
        String name = "Timmy"; //Value'en vi søker med //som blir satt inn
        String age = "12";
        String docKey = "Home";
        String destinationName = "Turn";
        String destinationAdress = "Gymsalen";
        String destinationCoords = "1234";

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


        Bruker bruker = new AdminBruker(name);
        ArrayList<Document> listOfDoc = new ArrayList<>();

///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
        /// THE DOING OF THE THING
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----

        MongoDBHandler mongodbhandler = new MongoDBHandler();

        //mongodbhandler.deleteOneDocument(name);
        //mongodbhandler.removeData(name, "Ajahs");
/*
        ///first field: "id":name
       mongodbhandler.sendData(string, bruker);
        ///second field: search by "id"-value, "age": age
       mongodbhandler.appendData(name, "age", age);
*/
        ///third field: list of dicts: "dict": dict
        // create a dict
        // add entries to doc
        // add entries to wrapping doc
        /// Is this better solved with a hashmap (NO IT IS NOT DONT DO IT)
        listDoc.append(docKey, destination); //dette gjøres ved hver kjøring og overskriver dermed listen

        mongodbhandler.appendData(name, "favorites", listDoc);  






    }
}
