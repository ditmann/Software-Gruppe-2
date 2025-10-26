package avandra.app;


import avandra.storage.adapter.MongoDBConnection;
import avandra.storage.adapter.MongoDBHandler;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Main {
    public static void main(String[] args) throws Exception {
/*

        MongoDBConnection connection = new MongoDBConnection();
        MongoDBHandler handler = new MongoDBHandler(connection);
        handler.createUser("roflmao", true);

        MongoDBConnection connection1 = new MongoDBConnection();
        MongoDBHandler handler1 = new MongoDBHandler(connection1);
        handler1.appendData("roflmao", "age", 12);*/

        MongoDBConnection conn = new MongoDBConnection();
        MongoCollection<Document> colldoc = conn.getCollection();
        System.out.println(colldoc);


    }
}

