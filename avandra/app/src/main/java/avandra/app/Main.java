package avandra.app;

import java.util.ArrayList;

import org.bson.Document;

import avandra.storage.adapter.MongoDBHandler;

public class Main {
    public static void main(String[] args) throws Exception {

        //lag key og value'
        String id = "id";
        String name = "Kåre";
        String age = "28";
        String docKey = "Hjem";
        String destinationName = "Hjem";
        String destinationAdress = "Dr. Ellertsens Vei";
        


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
        //you can sequentially create then add
        //if duplicates exist will add to first and no others
        String string = "id"; //Key'en vi søker etter
        String name = "Timmy"; //Value'en vi søker med //som blir satt inn
        String age = "12";
        String docKey = "Turn";
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

        Document newDestination = new Document();
        newDestination.append("Hjem", "Hjem");
        newDestination.append("Adresse", "Mammas hus 12");
        newDestination.append("Koordinater", "1223");

        Document anotherDestination = new Document();
        anotherDestination.append("Pappa", "Pappa");
        anotherDestination.append("Adresse", "Pappas hus 12");
        anotherDestination.append("Koordinater", "12344");


        Bruker bruker = new AdminBruker(name);
        ArrayList<Document> listOfDoc = new ArrayList<>();

///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----
        /// THE DOING OF THE THING
///  ----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^----|----^^*****^^--------^^*****^^----

        MongoDBHandler mongodbhandler = new MongoDBHandler();
/*
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


 */
            /*
        ArrayList<Document> data;
        data = mongodbhandler.retrieveAllData();
        for (Document doc : data) {
            System.out.println(doc);
        }
    }

}
*/

