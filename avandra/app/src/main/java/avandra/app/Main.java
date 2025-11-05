package avandra.app;


import avandra.storage.adapter.MongoDBConnectionPort;
import avandra.storage.adapter.MongoDBHandlerPort;

public class Main {
    public static void main(String[] args) throws Exception {
/*

        MongoDBConnectionPort connection = new MongoDBConnectionPort();
        MongoDBHandlerPort handler = new MongoDBHandlerPort(connection);
        handler.createUser("roflmao", true);

        MongoDBConnectionPort connection1 = new MongoDBConnectionPort();
        MongoDBHandlerPort handler1 = new MongoDBHandlerPort(connection1);
        handler1.appendData("roflmao", "age", 12);*/


        MongoDBConnectionPort connection1 = new MongoDBConnectionPort();
        MongoDBHandlerPort handler = new MongoDBHandlerPort(connection1);
        //handler.createUser("helo", true);
        //handler.appendData("helo", "age", 29);

        //handler.removeData("Timmys mor","addresse", "favoritter", "hjem");
        //handler.removeData("Timmys mor", "hjem", "favoritter");

    }
}

