package no.avandra;

import com.mongodb.client.FindIterable;
import no.avandra.classes.Coordinate;
import no.avandra.classes.Destinasjon;
import no.avandra.classes.*;
import org.bson.Document;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        //you can sequentially create then add
        //if duplicates exist will add to first and no others
        String name = "Galina Caspan";
        String age = "125";
        String firstAjah = "red";
        String secondAjah = "black";

        String string = "id";
        Bruker bruker = new AdminBruker(name);
        ArrayList<Document> listOfDoc = new ArrayList<>();

        MongoDBHandler mongodbhandler = new MongoDBHandler();



        ///first field: "id":name
        mongodbhandler.sendData(string, bruker);
        ///second field: search by "id"-value, "age": age
        mongodbhandler.appendData(name, "age", age);

        ///third field: list of dicts: "dict": dict
        // create a dict
        Document listDoc = new Document();
        Document doc = new Document();
        // add entries to doc
        doc.append("First Ajah", firstAjah);
        doc.append("Second Ajah", secondAjah);
        // add entries to wrapping doc
        listDoc.append(doc);

        mongodbhandler.appendData(name, "Ajahs", listDoc);


    }
}
