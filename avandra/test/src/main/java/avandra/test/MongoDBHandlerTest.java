package avandra.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;


import avandra.storage.adapter.MongoDBHandler;
import org.bson.Document;

import org.junit.jupiter.api.AfterEach;

import org.mockito.MockedStatic;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import com.mongodb.client.MongoDatabase;


class MongoDBHandlerTest {

    private final MongoDBHandler handler = new MongoDBHandler();

    @AfterEach
    void resetAccumulatingList() {
        handler.setList(new ArrayList<>());
    }

    /** Utility: wires MongoClients.create() -> client -> db -> collection */
    private static class utilityConnect {
        final MockedStatic<MongoClients> staticCreate;
        final MongoClient client = mock(MongoClient.class);
        final MongoDatabase db = mock(MongoDatabase.class);
        final MongoCollection<Document> coll = mock(MongoCollection.class);

        utilityConnect() {
            staticCreate = mockStatic(MongoClients.class);
            staticCreate.when(() -> MongoClients.create(anyString())).thenReturn(client);
            when(client.getDatabase(anyString())).thenReturn(db);
            when(db.getCollection(anyString())).thenReturn(coll);
        }

        void close() {
            staticCreate.close();
        }
    }

}
