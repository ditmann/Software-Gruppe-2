package avandra.test;

import avandra.storage.adapter.MongoDBConnectionAdapter;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoDBConnectionAdapterTest {

    @Test
    void open_opensDatabase() throws Exception {
        /// Tests the purpose of open(): to create a connection to the actual database.
        /// Proven by returning /something/ and not nothing
        MongoDBConnectionAdapter connection = new MongoDBConnectionAdapter(); //add params if applicable
        try (MongoDBConnectionAdapter newConnection = connection.open()) {
            MongoCollection<Document> collection = newConnection.getCollection();
            Assertions.assertNotNull(collection);
        }
    }

    @Test
    /// Testing that open() returns an instance of the connection-class that is equal to the original
    void open_returnsThis() throws Exception {
        MongoDBConnectionAdapter connection = new MongoDBConnectionAdapter();

        MongoDBConnectionAdapter newConnection = connection.open();
        Assertions.assertSame(connection, newConnection);
    }
    @Test
    /// Tests the logic in the open()-method - that it returns itself. Here: that mockito returns correct.
    void open_returnsMockedThis() throws Exception {
        MongoDBConnectionAdapter fakeConnection = mock(MongoDBConnectionAdapter.class);

        when(fakeConnection.open()).thenReturn(fakeConnection);
        MongoDBConnectionAdapter newConnection = fakeConnection.open();
        Assertions.assertEquals(fakeConnection, newConnection);
}
}

