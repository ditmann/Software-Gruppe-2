package no.avandra.classes;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javax.swing.text.Document;
/* code does not work yet
public class MongoDBHandler implements DBHandler {
    public void sendData(Object title, String content){
        MongoClient mongoClient = MongoClients.create("mongodb+srv://siljemst_db_user:Avandra1234567890@avandra.pix7etx.mongodb.net/dummy");
        MongoClient client = null;
        MongoDatabase db = client.getDatabase("dummy");
        MongoCollection<Document> collection = db.getCollection("testdata");
        collection.insertOne(new Document(title, content));
    }
    public Object retrieveData(){return 0;}
}
