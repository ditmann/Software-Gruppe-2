package no.avandra;

import no.avandra.classes.Coordinate;
import no.avandra.classes.Destinasjon;
import no.avandra.classes.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String string = "Hello World!";
        //Object object1 = new Object();
        Bruker bruker = new AdminBruker("Ida Granskog");

        MongoDBHandler mongodbhandler = new MongoDBHandler();

        mongodbhandler.sendData(bruker, string);
    }
}
