package avandra.test;

import avandra.storage.adapter.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoDBConnectionTest {

    @Test
    void open_opensDatabase() throws Exception {
        /// Tests the purpose of open(): to create a connection to the actual database.
        /// Proven by returning /something/ and not nothing
        MongoDBConnection connection = new MongoDBConnection(); //add params if applicable
        try (MongoDBConnection newConnection = connection.open()) {
            MongoCollection<Document> collection = newConnection.getCollection();
            Assertions.assertNotNull(collection);
        }
    }

    @Test
    /// Testing that open() returns an instance of the connection-class that is equal to the original
    void open_returnsThis() throws Exception {
        MongoDBConnection connection = new MongoDBConnection();

        MongoDBConnection newConnection = connection.open();
        Assertions.assertSame(connection, newConnection);
    }
    @Test
    /// Tests the logic in the open()-method - that it returns itself. Here: that mockito returns correct.
    void open_returnsMockedThis() throws Exception {
        MongoDBConnection fakeConnection = mock(MongoDBConnection.class);

        when(fakeConnection.open()).thenReturn(fakeConnection);
        MongoDBConnection newConnection = fakeConnection.open();
        Assertions.assertEquals(fakeConnection, newConnection);
}
}

