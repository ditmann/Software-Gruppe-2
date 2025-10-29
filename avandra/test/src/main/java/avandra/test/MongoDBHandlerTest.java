package avandra.test;

import java.util.ArrayList;
import java.util.Arrays;

import avandra.core.domain.Coordinate;
import avandra.core.port.DBConnection;
import avandra.storage.adapter.MongoDBConnection;
import org.bson.Document;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import avandra.storage.adapter.MongoDBHandler;

/**
 * Unit tests for MongoDBHandler adapter.
 *
 * These tests verify:
 *  - Correct MongoDB API calls (find, insert, update, delete)
 *  - Proper use of filters and updates
 *  - That MongoClient resources are always closed
 *  - Graceful error handling for MongoExceptions
 */
class MongoDBHandlerTest {

    /**
     * Utility: mocks the full chain
     * MongoClients.create() → MongoClient → MongoDatabase → MongoCollection.
     * Used by all tests to simplify setup.
     */
    private static class Wiring {
        final DBConnection rootConnection = mock(DBConnection.class);
        final MongoDBConnection openedConnection = mock(MongoDBConnection.class);
        final MongoCollection<Document> collection = mock(MongoCollection.class);
        final MongoDBHandler handler;

        Wiring() throws Exception {
            when(rootConnection.open()).thenReturn(openedConnection);
            when(openedConnection.getCollection()).thenReturn(collection);
            doNothing().when(openedConnection).close();
            handler = new MongoDBHandler(rootConnection);
        }
        private static final
        ArrayList<MongoDBHandler> INSTANCES_TO_RESET = new ArrayList<>();

        @AfterEach
        void cleanUpLists() {
            for (MongoDBHandler instance : INSTANCES_TO_RESET) {
                instance.setList(new ArrayList<>());
            }
            INSTANCES_TO_RESET.clear();
        }

        private void track_TrackingMongoDBHandlerObjects(Wiring w) {
            INSTANCES_TO_RESET.add(w.handler);

        }
    }

    // -----------------------------
    // createUser
    // -----------------------------

    /**
     * createUser(): verifies that a single document is inserted
     * with the expected key/value, and that the MongoClient is closed.
     */

    @Test
    void createUser_insertsOneDocument() throws Exception {
        Wiring w = new Wiring();
        w.track_TrackingMongoDBHandlerObjects(w);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> mongoCursor = mock(MongoCursor.class);

        when(w.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(mongoCursor);
        when(mongoCursor.hasNext()).thenReturn(false);

        w.handler.createUser("Kjell",true);

        verify(w.collection, times(1)).insertOne(ArgumentMatchers.argThat(d ->
                "Kjell".equals(d.getString("id")) && d.containsKey("admin") && d.containsKey("litebrukere")
                && d.containsKey("favoritter") && d.containsKey("planlagte reiser")));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    @Test
    void searchDestination_insertDestinationInFavorites() throws Exception {

        Wiring w = new Wiring();
        w.track_TrackingMongoDBHandlerObjects(w);
        Document coordsDoc = new Document("latitude", 59.3231).append("longitude", 11.2526);
        Document destinationDoc = new Document("koordinater", coordsDoc);
        Document favoritesDoc = new Document("FFK", destinationDoc);
        Document userDoc = new Document("id", "Kåre").append("favoritter", favoritesDoc);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(w.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(userDoc);
        Coordinate coordinateResults = w.handler.searchDestination("Kåre", "FFK");


        when(w.collection.find(any(Bson.class))).thenReturn(findIterable);

        assertNotNull(coordinateResults, "expected coordinates");
        assertEquals(59.3231, coordinateResults.getLatitudeNum());
        assertEquals(11.2526, coordinateResults.getLongitudeNUM());

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();

    }
}
