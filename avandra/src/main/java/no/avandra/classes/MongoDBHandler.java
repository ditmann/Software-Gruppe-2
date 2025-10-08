package no.avandra.classes;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// code does not work yet
public class MongoDBHandler implements DBHandler {
    public void sendData(Object title, String content){
        MongoClient mongoClient = MongoClients.create("mongodb+srv://siljemst_db_user:Avandra1234567890@avandra.pix7etx.mongodb.net/dummy");
        //MongoClient client = null;
        MongoDatabase db = mongoClient.getDatabase("dummy");
        MongoCollection<Document> collection = db.getCollection("testdata");
        collection.insertOne(new Document(title.toString(), content));
    }
    public Object retrieveData(){return 0;

    }

/*
 [
  {
    "id":"1",
    "user_id":"kare_demo",'
    "destinasjon":"Hjem",
    "adresse":"Dr. Ellertsens Vei",
    "latitudeNum":59.13555,
    "longitudeNUM":10.59498
  }
]
*/
}
