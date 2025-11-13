// These tests have gone through a lot of iterations, they were written a bit too early
// and maybe also a bit too complicated than what they needed to be, here is the final
// version
package avandra.test;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.DBConnectionPort;
import avandra.storage.adapter.MongoDBConnectionAdapter;
import avandra.storage.adapter.MongoDBHandlerAdapter;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * Unit tests for the MongoDB adapter using mocked database calls
 * Tests create, read, update, and delete for users and destinations
 * Checks that MongoException is caught and does not crash the program
 * Makes sure missing data just returns empty results instead of errors
 * Prevents data from leaking between method calls
 */

class MongoDBHandlerAdapterTest {

    private static BsonDocument toDoc(Bson bson) {
        return bson.toBsonDocument(Document.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
    }

    /** Small wiring helper so each test works with a clean set of mocks */
    private static class TestWiring {
        final DBConnectionPort rootConnection = mock(DBConnectionPort.class);
        final MongoDBConnectionAdapter openedConnection = mock(MongoDBConnectionAdapter.class);
        final MongoCollection<Document> collection = mock(MongoCollection.class);
        final MongoDBHandlerAdapter handler;

        TestWiring() throws Exception {
            when(rootConnection.open()).thenReturn(openedConnection);
            when(openedConnection.getCollection()).thenReturn(collection);
            doNothing().when(openedConnection).close();
            handler = new MongoDBHandlerAdapter(rootConnection);
        }
    }

    // Per test wiring so we can verify close() in @AfterEach without repeating ourselves
    private TestWiring wiring;
    // Some tests use custom wiring for example the MongoException
    private boolean skipCloseVerification;

    @BeforeEach
    void setUp() throws Exception {
        wiring = new TestWiring();
        skipCloseVerification = false;
    }

    @AfterEach
    void tearDown() throws Exception {
        if (!skipCloseVerification && wiring != null) {
            verify(wiring.openedConnection).close();
        }
    }

    @Test
    void createUser_inserts_whenIdFree() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);

        wiring.handler.createUser("u1", true);

        verify(wiring.collection, times(1)).insertOne(
                argThat(doc ->
                        "u1".equals(doc.getString("id")) &&
                                doc.containsKey("admin") &&
                                doc.containsKey("litebrukere") &&
                                doc.containsKey("planlagte reiser") &&
                                doc.containsKey("favoritter")
                )
        );
    }

    @Test
    void retrieveAllData_returnsAll() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        Document doc1 = new Document("a", 1);
        Document doc2 = new Document("b", 2);

        when(wiring.collection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);

        ArrayList<Document> result = wiring.handler.retrieveAllData();

        assertEquals(2, result.size());
        assertTrue(result.contains(doc1));
        assertTrue(result.contains(doc2));
    }

    @Test
    void retrieveByKeyValue_returnsMatches() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> probeCursor = mock(MongoCursor.class);
        MongoCursor<Document> loopCursor = mock(MongoCursor.class);

        Document matchedDoc = new Document("key", "val");

        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(probeCursor, loopCursor);
        when(probeCursor.hasNext()).thenReturn(true);
        when(loopCursor.hasNext()).thenReturn(true, false);
        when(loopCursor.next()).thenReturn(matchedDoc);

        ArrayList<Document> result = wiring.handler.retrieveByKeyValue("key", "val");

        assertEquals(1, result.size());
        assertEquals("val", result.get(0).getString("key"));
    }

    @Test
    void retrieveByValue_matchesOnKeyOrValue() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        Document valueMatch = new Document("field1", "needle");
        Document keyMatch   = new Document("needle", "anything");
        Document noMatch    = new Document("other", "stuff");

        when(wiring.collection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn(valueMatch, keyMatch, noMatch);

        ArrayList<Document> result = wiring.handler.retrieveByValue("needle");

        assertEquals(2, result.size());
        assertTrue(result.contains(valueMatch));
        assertTrue(result.contains(keyMatch));
    }

    @Test
    void appendData_setsField() throws Exception {
        when(wiring.collection.countDocuments(any(Bson.class))).thenReturn(1L);
        UpdateResult updateResult = mock(UpdateResult.class);
        when(wiring.collection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updateResult);

        wiring.handler.setIdField("id");
        wiring.handler.appendData("Per", "age", "78");

        ArgumentCaptor<Bson> filterCaptor = ArgumentCaptor.forClass(Bson.class);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        verify(wiring.collection).updateOne(filterCaptor.capture(), updateCaptor.capture());

        BsonDocument filterDoc = toDoc(filterCaptor.getValue());
        BsonDocument updateDoc = toDoc(updateCaptor.getValue());
        assertEquals("Per", filterDoc.getString("id").getValue());
        assertEquals("78", updateDoc.getDocument("$set").getString("age").getValue());
    }

    @Test
    void removeData_unset_updatesWhenUserExists() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true);
        when(wiring.collection.updateOne(any(Bson.class), any(Bson.class)))
                .thenReturn(mock(UpdateResult.class));

        wiring.handler.removeData("user-123", "obsolete");

        verify(wiring.collection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    void removeData_delete_deletesWhenExists() throws Exception {
        when(wiring.collection.countDocuments(any(Bson.class))).thenReturn(1L);
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(wiring.collection.deleteOne(any(Bson.class))).thenReturn(deleteResult);

        wiring.handler.removeData("user-123");

        verify(wiring.collection).deleteOne(any(Bson.class));
    }

    @Test
    void searchFavDestination_returnsCoordinates() throws Exception {
        Document coordDoc = new Document("latitude", 59.3231).append("longitude", 11.2526);
        Document destinationDoc = new Document("koordinater", coordDoc);
        Document favoritesDoc = new Document("FFK", destinationDoc);
        Document userDoc = new Document("id", "Kåre").append("favoritter", favoritesDoc);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(userDoc);

        CoordinateDTO coordinates = wiring.handler.searchFavDestination("Kåre", "FFK");

        assertNotNull(coordinates);
        assertEquals(59.3231, coordinates.getLatitudeNum(), 1e-6);
        assertEquals(11.2526, coordinates.getLongitudeNUM(), 1e-6);
    }

    @Test
    void listUserFavDestinations_returnsNamesOnly() throws Exception {
        Document favoritesDoc = new Document("Home", new Document())
                .append("School", new Document("koordinater", new Document("latitude", 1.0).append("longitude", 2.0)))
                .append("Kiosk", new Document());

        Document userDoc = new Document("id", "u1").append("favoritter", favoritesDoc);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(userDoc);

        List<String> names = wiring.handler.listUserFavDestinations("u1");

        assertEquals(List.of("Home", "School", "Kiosk"), names);
    }


    @Test
    void listLitebrukereForAdmin_singleString() throws Exception {
        Document adminDoc = new Document("id", "a").append("litebrukere", "kid-1");
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(adminDoc);

        List<String> litebrukere = wiring.handler.listLitebrukereForAdmin("a");
        assertEquals(List.of("kid-1"), litebrukere);
    }

    @Test
    void listLitebrukereForAdmin_listWithBlank() throws Exception {
        Document adminDoc = new Document("id", "a").append("litebrukere", List.of("kid-1", " ", "kid-2"));
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(adminDoc);

        List<String> litebrukere = wiring.handler.listLitebrukereForAdmin("a");
        assertEquals(List.of("kid-1", "kid-2"), litebrukere);
    }

    @Test
    void listLitebrukereForAdmin_missingField() throws Exception {
        Document adminDoc = new Document("id", "a");
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(adminDoc);

        List<String> litebrukere = wiring.handler.listLitebrukereForAdmin("a");
        assertTrue(litebrukere.isEmpty());
    }


    @Test
    void isAdmin_true() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(new Document("id", "a").append("admin", true));

        assertTrue(wiring.handler.isAdmin("a"));
    }

    @Test
    void isAdmin_false() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(new Document("id", "b"));

        assertFalse(wiring.handler.isAdmin("b"));
    }

    @Test
    void addDestinationToFavorites_callsUpdateOne() throws Exception {
        when(wiring.collection.updateOne(any(Bson.class), any(Bson.class)))
                .thenReturn(mock(UpdateResult.class));

        wiring.handler.addDestinationToFavorites("u1", "Park", "Main St 1", 10.0, 20.0);

        verify(wiring.collection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    void addCoordinatesToFavDestination_callsUpdateOne() throws Exception {
        when(wiring.collection.updateOne(any(Bson.class), any(Bson.class)))
                .thenReturn(mock(UpdateResult.class));

        wiring.handler.addCoordinatesToFavDestination("u1", "School", 59.9, 10.7);

        verify(wiring.collection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    void deleteManyDocuments_deletesWhenCountPositive() throws Exception {
        when(wiring.collection.countDocuments(any(Bson.class))).thenReturn(2L);
        when(wiring.collection.deleteMany(any(Bson.class))).thenReturn(mock(DeleteResult.class));

        wiring.handler.deleteManyDocuments("dup");

        verify(wiring.collection).deleteMany(any(Bson.class));
    }

    @Test
    void deleteManyDocuments_doesNothingWhenZero() throws Exception {
        when(wiring.collection.countDocuments(any(Bson.class))).thenReturn(0L);

        wiring.handler.deleteManyDocuments("none");

        verify(wiring.collection, never()).deleteMany(any(Bson.class));
    }

    @Test
    void searchFavDestination_returnsNull_whenUserMissing() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        assertNull(wiring.handler.searchFavDestination("u1", "Home"));
    }

    @Test
    void searchFavDestination_returnsNull_whenFavoritesMissing() throws Exception {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(new Document("id", "u1"));

        assertNull(wiring.handler.searchFavDestination("u1", "Home"));
    }

    @Test
    void searchDestination_returnsNull_whenFavDestinationWithoutCoords() throws Exception {
        Document favorites = new Document("Home", new Document()); // no koordinater
        Document user = new Document("id", "u1").append("favoritter", favorites);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(wiring.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(user);

        assertNull(wiring.handler.searchFavDestination("u1", "Home"));
    }

    @Test
    void appendData_doesNothingWhenUserNotFound() throws Exception {
        when(wiring.collection.countDocuments(any(Bson.class))).thenReturn(0L);

        wiring.handler.appendData("uX", "age", 30);

        verify(wiring.collection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    void removeData_delete_doesNothingWhenCountZero() throws Exception {
        when(wiring.collection.countDocuments(any(Bson.class))).thenReturn(0L);

        wiring.handler.removeData("uX");

        verify(wiring.collection, never()).deleteOne(any(Bson.class));
    }

    @Test
    void methods_handleMongoException() throws Exception {
        // Intentional sweep test verifies that all public methods handle MongoException
        // from getCollection() without throwing Keeps behavior stable across refactors
        skipCloseVerification = true; // uses its own wiring dont verify the class level close() here

        DBConnectionPort rootConnection = mock(DBConnectionPort.class);
        MongoDBConnectionAdapter openedConnection = mock(MongoDBConnectionAdapter.class);
        when(rootConnection.open()).thenReturn(openedConnection);
        when(openedConnection.getCollection()).thenThrow(new MongoException("boom"));
        doNothing().when(openedConnection).close();

        MongoDBHandlerAdapter handler = new MongoDBHandlerAdapter(rootConnection);

        assertDoesNotThrow(() -> handler.createUser("x", true));
        assertDoesNotThrow(handler::retrieveAllData);
        assertDoesNotThrow(() -> handler.retrieveByKeyValue("k", "v"));
        assertDoesNotThrow(() -> handler.retrieveByValue("needle"));
        assertDoesNotThrow(() -> handler.appendData("x", "a", "b"));
        assertDoesNotThrow(() -> handler.removeData("x", "y"));
        assertDoesNotThrow(() -> handler.removeData("x"));
        assertDoesNotThrow(() -> handler.deleteManyDocuments("x"));
        assertDoesNotThrow(() -> handler.addDestinationToFavorites("x", "Park", "Main St 1", 10.0, 20.0));
        assertDoesNotThrow(() -> handler.addCoordinatesToFavDestination("x", "Park", 10.0, 20.0));
        assertDoesNotThrow(() -> handler.searchFavDestination("x", "dest"));
        assertDoesNotThrow(() -> handler.listUserFavDestinations("x"));
        assertDoesNotThrow(() -> handler.listLitebrukereForAdmin("x"));
        assertDoesNotThrow(() -> handler.isAdmin("x"));

        verify(rootConnection, atLeastOnce()).open();
        verify(openedConnection, atLeastOnce()).close();
    }
}
