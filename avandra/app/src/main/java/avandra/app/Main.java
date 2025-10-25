package avandra.app;


import avandra.storage.adapter.MongoDBConnection;
import avandra.storage.adapter.MongoDBHandler;

public class Main {
    public static void main(String[] args) throws Exception {


        MongoDBConnection connection = new MongoDBConnection();


        MongoDBHandler dbHandler = new MongoDBHandler(connection);

        dbHandler.createUser("helo", true);


    }
}

