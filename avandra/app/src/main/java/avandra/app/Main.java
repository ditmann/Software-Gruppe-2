package avandra.app;


import avandra.storage.adapter.MongoDBConnection;
import avandra.storage.adapter.MongoDBHandler;

public class Main {
    public static void main(String[] args) throws Exception {


        MongoDBConnection connection = new MongoDBConnection();
        MongoDBHandler handler = new MongoDBHandler(connection);
        handler.createUser("roflmao", true);

        MongoDBConnection connection1 = new MongoDBConnection();
        MongoDBHandler handler1 = new MongoDBHandler(connection1);
        handler1.appendData("roflmao", "age", 12);


    }
}

