package no.avandra;

import com.mongodb.client.FindIterable;
import no.avandra.classes.Coordinate;
import no.avandra.classes.Destinasjon;
import no.avandra.classes.*;
import org.bson.Document;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        /*
        //exampledata
        String string = "Hello World!";
        Bruker bruker = new AdminBruker("Ida Granskog");

        MongoDBHandler mongodbhandler = new MongoDBHandler();

        mongodbhandler.sendData(bruker, string);

        //another
        String string1 = "Let's go concat!";
        Bruker bruker1 = new AdminBruker("Fang Runin");

        MongoDBHandler mongodbh = new MongoDBHandler();
        mongodbh.sendData(bruker1, string1);
        */

        //tryna search for something
        MongoDBHandler mongodbhandler = new MongoDBHandler();
        ArrayList<Document> charlie = mongodbhandler.searchByKeyValue("Ida Granskog", "Hello World!");
        for (Document iterable : charlie) {
            System.out.println(iterable);
        }

        //tryna get it all out
        ArrayList<Document> S = mongodbhandler.retrieveAllData();
        for (Document doc : S) {
            System.out.println("\n" + doc);

        }

    }
}
