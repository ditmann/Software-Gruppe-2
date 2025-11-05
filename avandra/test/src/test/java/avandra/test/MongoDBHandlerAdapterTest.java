package avandra.test;

import java.util.ArrayList;
import java.util.Arrays;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.DBConnectionPort;
import avandra.storage.adapter.MongoDBConnectionAdapter;
import avandra.storage.adapter.MongoDBHandlerAdapter;
import org.bson.Document;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for MongoDBHandlerAdapter.
 *
 * We:
 *  - mock DBConnectionPort -> MongoDBConnectionAdapter -> MongoCollection so we never hit a real DB
 *  - mock FindIterable + MongoCursor for reads/loops
 *
 * IMPORTANT: all @Test methods declare `throws Exception`
 * because some handler methods are declared with checked throws.
 */
class MongoDBHandlerAdapterTest {

    /**
     * Wiring object bundles all mocks and a handler instance.
     *
     * rootConnection.open() -> openedConnection
     * openedConnection.getCollection() -> collection
     * openedConnection.close() must be allowed
     */
    private static class Wiring {
        final DBConnectionPort rootConnection = mock(DBConnectionPort.class);
        final MongoDBConnectionAdapter openedConnection = mock(MongoDBConnectionAdapter.class);
        final MongoCollection<Document> collection = mock(MongoCollection.class);
        final MongoDBHandlerAdapter handler;

        Wiring() throws Exception {
            when(rootConnection.open()).thenReturn(openedConnection);
            when(openedConnection.getCollection()).thenReturn(collection);
            doNothing().when(openedConnection).close();

            handler = new MongoDBHandlerAdapter(rootConnection);
        }
    }

    /**
     * The handler accumulates results in an internal list.
     * Clear it after each test to prevent cross-test contamination.
     */
    private static final ArrayList<MongoDBHandlerAdapter> INSTANCES_TO_RESET = new ArrayList<>();

    @AfterEach
    void cleanupLists() {
        for (MongoDBHandlerAdapter h : INSTANCES_TO_RESET) {
            h.setList(new ArrayList<>());
        }
        INSTANCES_TO_RESET.clear();
    }

    private void track(Wiring w) {
        INSTANCES_TO_RESET.add(w.handler);
    }

    // -----------------------------
    // createUser
    // -----------------------------

    @Test
    void createUser_insertsOneDocument_whenIdDoesNotExist() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        when(w.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false); // means "ID is free"

        w.handler.createUser("bar", true);

        verify(w.collection, times(1)).insertOne(
                argThat(d ->
                        "bar".equals(d.getString("id")) &&
                                d.containsKey("admin") &&
                                d.containsKey("litebrukere") &&
                                d.containsKey("planlagte reiser") &&
                                d.containsKey("favoritter")
                )
        );

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // retrieveAllData
    // -----------------------------

    @Test
    void retrieveAllData_returnsAllDocs() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        Document d1 = new Document("a", 1);
        Document d2 = new Document("b", 2);

        when(w.collection.find()).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(cursor);

        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(d1, d2);

        ArrayList<Document> result = w.handler.retrieveAllData();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(d1, d2)));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // retrieveByKeyValue
    // -----------------------------

    @Test
    void retrieveByKeyValue_filtersByEq() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> iterable = mock(FindIterable.class);

        MongoCursor<Document> probeCursor = mock(MongoCursor.class);
        MongoCursor<Document> loopCursor = mock(MongoCursor.class);

        Document d = new Document("key", "val");

        when(w.collection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(probeCursor, loopCursor);

        // probeCursor used for `hasNext()` check
        when(probeCursor.hasNext()).thenReturn(true);

        // loopCursor used in the enhanced-for
        when(loopCursor.hasNext()).thenReturn(true, false);
        when(loopCursor.next()).thenReturn(d);

        ArrayList<Document> result = w.handler.retrieveByKeyValue("key", "val");

        // Verify that Filters.eq("key", "val") was used
        verify(w.collection).find(
                ArgumentMatchers.<Bson>argThat(b -> {
                    CodecRegistry registry = CodecRegistries.fromRegistries(
                            com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                            CodecRegistries.fromProviders(new DocumentCodecProvider())
                    );
                    var doc = b.toBsonDocument(Document.class, registry);
                    return doc != null
                            && doc.getString("key") != null
                            && "val".equals(doc.getString("key").getValue());
                })
        );

        assertEquals(1, result.size());
        assertEquals("val", result.get(0).getString("key"));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // appendData
    // -----------------------------

    @Test
    void appendData_setsField_forUser() throws Exception {
        Wiring w = new Wiring();
        track(w);

        when(w.collection.countDocuments(any(Bson.class))).thenReturn(1L);

        UpdateResult updResult = mock(UpdateResult.class);
        when(w.collection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updResult);

        w.handler.setIdField("id");
        w.handler.appendData("Per", "age", "78");

        ArgumentCaptor<Bson> filterCap = ArgumentCaptor.forClass(Bson.class);
        ArgumentCaptor<Bson> updateCap = ArgumentCaptor.forClass(Bson.class);
        verify(w.collection).updateOne(filterCap.capture(), updateCap.capture());

        CodecRegistry registry = CodecRegistries.fromRegistries(
                com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(new DocumentCodecProvider())
        );

        var filterDoc = filterCap.getValue().toBsonDocument(Document.class, registry);
        var updateDoc = updateCap.getValue().toBsonDocument(Document.class, registry);

        assertEquals("Per", filterDoc.getString("id").getValue());

        var setDoc = updateDoc.getDocument("$set");
        assertNotNull(setDoc);
        assertEquals("78", setDoc.getString("age").getValue());

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // removeData (unset field)
    // -----------------------------

    @Test
    void removeData_unsetsKey_ifUserExists() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        when(w.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true);

        UpdateResult updRes = mock(UpdateResult.class);
        when(w.collection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updRes);

        w.handler.removeData("user-123", "obsoleteField");

        verify(w.collection).updateOne(any(Bson.class), any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // removeData (deleteOne by id)
    // -----------------------------

    @Test
    void removeData_deletesDocById_ifExists() throws Exception {
        Wiring w = new Wiring();
        track(w);

        when(w.collection.countDocuments(any(Bson.class))).thenReturn(1L);

        DeleteResult del = mock(DeleteResult.class);
        when(del.getDeletedCount()).thenReturn(1L);
        when(w.collection.deleteOne(any(Bson.class))).thenReturn(del);

        w.handler.removeData("user-123");

        verify(w.collection).deleteOne(any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // deleteManyDocuments
    // -----------------------------

    @Test
    void deleteManyDocuments_deletesAllWithSameId_ifExists() throws Exception {
        Wiring w = new Wiring();
        track(w);

        when(w.collection.countDocuments(any(Bson.class))).thenReturn(3L);

        DeleteResult del = mock(DeleteResult.class);
        when(del.getDeletedCount()).thenReturn(3L);
        when(w.collection.deleteMany(any(Bson.class))).thenReturn(del);

        w.handler.deleteManyDocuments("dup-id");

        verify(w.collection).deleteMany(any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // retrieveByValue
    // -----------------------------

    @Test
    void retrieveByValue_matchesOnKeyOrValue() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        Document valueMatch = new Document("field1", "needle");
        Document keyMatch   = new Document("needle", "anything");
        Document noMatch    = new Document("other", "stuff");

        when(w.collection.find()).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(cursor);

        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn(valueMatch, keyMatch, noMatch);

        var result = w.handler.retrieveByValue("needle");

        assertEquals(2, result.size());
        assertTrue(result.contains(valueMatch));
        assertTrue(result.contains(keyMatch));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // insertDestinationForLiteUser
    // -----------------------------

    @Test
    void insertDestinationForLiteUser_twoUpdates_returnBasedOnSecondUpdateCount() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> adminFind = mock(FindIterable.class);

        when(w.collection.find(any(Bson.class))).thenReturn(adminFind);
        when(adminFind.first()).thenReturn(new Document("id", "admin1"));

        UpdateResult firstUpdate = mock(UpdateResult.class);
        when(firstUpdate.getModifiedCount()).thenReturn(1L);

        UpdateResult secondUpdate = mock(UpdateResult.class);
        when(secondUpdate.getModifiedCount()).thenReturn(1L);

        when(w.collection.updateOne(any(Bson.class), any(Bson.class)))
                .thenReturn(firstUpdate)
                .thenReturn(secondUpdate);

        boolean res = w.handler.insertDestinationForLiteUser(
                "lite1", "dest1", "Home", "Addr", 59.9, 10.7, "admin1");

        assertTrue(res);
        verify(w.collection, times(2)).updateOne(any(Bson.class), any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    @Test
    void insertDestinationForLiteUser_adminNull_stillReturnsBasedOnSecondUpdate() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> adminFind = mock(FindIterable.class);

        when(w.collection.find(any(Bson.class))).thenReturn(adminFind);
        when(adminFind.first()).thenReturn(null);

        UpdateResult firstUpdate = mock(UpdateResult.class);
        when(firstUpdate.getModifiedCount()).thenReturn(0L);

        UpdateResult secondUpdate = mock(UpdateResult.class);
        when(secondUpdate.getModifiedCount()).thenReturn(1L);

        when(w.collection.updateOne(any(Bson.class), any(Bson.class)))
                .thenReturn(firstUpdate)
                .thenReturn(secondUpdate);

        boolean res = w.handler.insertDestinationForLiteUser(
                "lite1", "dest1", "Gym", "Addr3", 59.0, 10.0, "admin");

        assertTrue(res);
        verify(w.collection, times(2)).updateOne(any(Bson.class), any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // searchDestination
    // -----------------------------

    @Test
    void searchDestination_returnsCoordinates_whenFavoriteHasCoords() throws Exception {
        Wiring w = new Wiring();
        track(w);

        Document coordsDoc = new Document("latitude", 59.3231)
                .append("longitude", 11.2526);
        Document destinationDoc = new Document("koordinater", coordsDoc);
        Document favoritesDoc = new Document("FFK", destinationDoc);
        Document userDoc = new Document("id", "Kåre")
                .append("favoritter", favoritesDoc);

        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(w.collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(userDoc);

        CoordinateDTO result = w.handler.searchDestination("Kåre", "FFK");

        assertNotNull(result, "Expected coordinates, got null");
        assertEquals(59.3231, result.getLatitudeNum(), 1e-6);
        assertEquals(11.2526, result.getLongitudeNUM(), 1e-6);

        verify(w.collection).find(any(Bson.class));
        verify(w.rootConnection).open();
        verify(w.openedConnection).close();
    }

    // -----------------------------
    // Exception handling
    // -----------------------------

    @Test
    void methods_handleMongoException_gracefully() throws Exception {

        DBConnectionPort rootConn = mock(DBConnectionPort.class);
        MongoDBConnectionAdapter openedConn = mock(MongoDBConnectionAdapter.class);

        when(rootConn.open()).thenReturn(openedConn);
        when(openedConn.getCollection()).thenThrow(new MongoException("error"));
        doNothing().when(openedConn).close();

        MongoDBHandlerAdapter handler = new MongoDBHandlerAdapter(rootConn);

        assertDoesNotThrow(() -> handler.searchDestination("Per", "Hjem"));
        assertDoesNotThrow(() -> handler.createUser("Per", true));
        assertDoesNotThrow(() -> handler.retrieveAllData());
        assertDoesNotThrow(() -> handler.retrieveByKeyValue("Tore", "Hjem"));
        assertDoesNotThrow(() -> handler.appendData("Per", "Hjem", "Fem"));
        assertDoesNotThrow(() -> handler.removeData("Per", "hjem"));
        assertDoesNotThrow(() -> handler.insertDestinationForLiteUser(
                "lite", "d", "n", "a", 1.0, 2.0, "admin"));

        verify(rootConn, atLeastOnce()).open();
        verify(openedConn, atLeastOnce()).close();
    }
}
