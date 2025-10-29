package avandra.test;

import java.util.ArrayList;
import java.util.Arrays;

import avandra.core.domain.Coordinate;
import avandra.core.port.DBConnection;
import avandra.storage.adapter.MongoDBConnection;
import avandra.storage.adapter.MongoDBHandler;
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
 * Unit tests for MongoDBHandler.
 *
 * We:
 *  - mock DBConnection and MongoDBConnection so we never hit a real DB
 *  - mock MongoCollection<Document>
 *  - mock FindIterable + MongoCursor for reads
 *
 * IMPORTANT: all @Test methods declare `throws Exception`
 * because some handler methods are declared with checked throws.
 */
class MongoDBHandlerTest {

    private static class Wiring {
        final DBConnection rootConnection = mock(DBConnection.class);
        final MongoDBConnection oppnedConnection = mock(MongoDBConnection.class);
        final MongoCollection<Document> collection = mock(MongoCollection.class);
        final MongoDBHandler handler;

        Wiring() throws Exception {
            when(rootConnection.open()).thenReturn(oppnedConnection);
            when(oppnedConnection.getCollection()).thenReturn(collection);
            doNothing().when(oppnedConnection).close(); // try-with-resources safety

            handler = new MongoDBHandler(rootConnection);
        }
    }

    // We create fresh Wiring per test, so we don't need a global @BeforeEach.
    // But MongoDBHandler internally stores results in a list, so we
    // clear that list after each test to avoid leaking results across tests.
    private static final ArrayList<MongoDBHandler> INSTANCES_TO_RESET = new ArrayList<>();

    @AfterEach
    void cleanupLists() {
        for (MongoDBHandler h : INSTANCES_TO_RESET) {
            h.setList(new ArrayList<>());
        }
        INSTANCES_TO_RESET.clear();
    }

    // helper: after creating Wiring w in a test, call track(w) so cleanupLists() can reset it.
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
                ArgumentMatchers.argThat(d ->
                        "bar".equals(d.getString("id")) &&
                                d.containsKey("admin") &&
                                d.containsKey("litebrukere") &&
                                d.containsKey("planlagte reiser") &&
                                d.containsKey("favoritter")
                )
        );

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
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
        verify(w.oppnedConnection).close();
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

        // verify the equality filter { key: "val" } was generated
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
        verify(w.oppnedConnection).close();
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

        // update should be { $set: { age: "78" } }
        var setDoc = updateDoc.getDocument("$set");
        assertNotNull(setDoc);
        assertEquals("78", setDoc.getString("age").getValue());

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
    }

    // -----------------------------
    // removeData (unset field)
    // -----------------------------

    @Test
    void removeData_unsetsKey_exactFilterAndUpdate() throws Exception {
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

        CodecRegistry registry = CodecRegistries.fromRegistries(
                com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(new DocumentCodecProvider())
        );

        verify(w.collection).updateOne(
                // filter { id: "user-123" }
                ArgumentMatchers.<Bson>argThat(filter -> {
                    var doc = filter.toBsonDocument(Document.class, registry);
                    return doc != null
                            && doc.getString("id") != null
                            && "user-123".equals(doc.getString("id").getValue());
                }),
                // update { $unset: { obsoleteField: "" } }
                ArgumentMatchers.<Bson>argThat(update -> {
                    var doc = update.toBsonDocument(Document.class, registry);
                    var unset = (doc != null) ? doc.getDocument("$unset") : null;
                    return unset != null && unset.containsKey("obsoleteField");
                })
        );

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
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

        verify(w.collection).deleteOne(ArgumentMatchers.<Bson>argThat(b -> {
            CodecRegistry reg = CodecRegistries.fromRegistries(
                    com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(new DocumentCodecProvider()));
            var doc = b.toBsonDocument(Document.class, reg);
            return doc.getString("id") != null
                    && "user-123".equals(doc.getString("id").getValue());
        }));

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
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

        verify(w.collection).deleteMany(ArgumentMatchers.<Bson>argThat(b -> {
            CodecRegistry reg = CodecRegistries.fromRegistries(
                    com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(new DocumentCodecProvider()));
            var doc = b.toBsonDocument(Document.class, reg);
            return doc.getString("id") != null
                    && "dup-id".equals(doc.getString("id").getValue());
        }));

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
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
        Document keyMatch = new Document("needle", "anything");
        Document noMatch = new Document("other", "stuff");

        when(w.collection.find()).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(cursor);

        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn(valueMatch, keyMatch, noMatch);

        var result = w.handler.retrieveByValue("needle");

        assertEquals(2, result.size());
        assertTrue(result.contains(valueMatch));
        assertTrue(result.contains(keyMatch));

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
    }

    // -----------------------------
    // insertDestinationForLiteUser
    // -----------------------------
    @Test
    void insertDestinationForLiteUser_twoUpdates_returnBasedOnSecondUpdateCount() throws Exception {
        Wiring w = new Wiring();
        track(w);

        FindIterable<Document> adminFind = mock(FindIterable.class);

        // case A: admin exists
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

        assertTrue(res); // because secondUpdate.getModifiedCount() == 1
        verify(w.collection, times(2)).updateOne(any(Bson.class), any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
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

        assertTrue(res); // still true because method uses second update result
        verify(w.collection, times(2)).updateOne(any(Bson.class), any(Bson.class));

        verify(w.rootConnection).open();
        verify(w.oppnedConnection).close();
    }
    // -----------------------------
    // searchDestination
    // -----------------------------
    @Test
    void searchDestination_insertDestinationInFavorites() throws Exception {

        Wiring w = new Wiring();
        track(w);
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
        verify(w.oppnedConnection).close();

    }

    // -----------------------------
    // Exception handling
    // -----------------------------

    @Test
    void methods_handleMongoException_gracefully() throws Exception {

        DBConnection rootConn = mock(DBConnection.class);
        MongoDBConnection openedConn = mock(MongoDBConnection.class);

        when(rootConn.open()).thenReturn(openedConn);
        when(openedConn.getCollection()).thenThrow(new MongoException("error"));
        doNothing().when(openedConn).close();

        MongoDBHandler handler = new MongoDBHandler(rootConn);

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
