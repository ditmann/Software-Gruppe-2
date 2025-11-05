package avandra.test;

import avandra.storage.adapter.MongoDBConnectionPort;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoDBConnectionPortTest {

    @Test
    void open_opensDatabase() throws Exception {
        /// Tests the purpose of open(): to create a connection to the actual database.
        /// Proven by returning /something/ and not nothing
        MongoDBConnectionPort connection = new MongoDBConnectionPort(); //add params if applicable
        try (MongoDBConnectionPort newConnection = connection.open()) {
            MongoCollection<Document> collection = newConnection.getCollection();
            Assertions.assertNotNull(collection);
        }
    }

    @Test
    /// Testing that open() returns an instance of the connection-class that is equal to the original
    void open_returnsThis() throws Exception {
        MongoDBConnectionPort connection = new MongoDBConnectionPort();

        MongoDBConnectionPort newConnection = connection.open();
        Assertions.assertSame(connection, newConnection);
    }
    @Test
    /// Tests the logic in the open()-method - that it returns itself. Here: that mockito returns correct.
    void open_returnsMockedThis() throws Exception {
        MongoDBConnectionPort fakeConnection = mock(MongoDBConnectionPort.class);

        when(fakeConnection.open()).thenReturn(fakeConnection);
        MongoDBConnectionPort newConnection = fakeConnection.open();
        Assertions.assertEquals(fakeConnection, newConnection);
}
}

