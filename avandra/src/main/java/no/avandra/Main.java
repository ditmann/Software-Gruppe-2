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
        String string = "id";
        Bruker bruker = new AdminBruker("Moiraine Damodred");

        MongoDBHandler mongodbhandler = new MongoDBHandler();

        mongodbhandler.sendData(string, bruker);

        mongodbhandler.appendData("Moiraine Damodred", "age", "42");
    }
}
