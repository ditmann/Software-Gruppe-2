package avandra.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;

import avandra.storage.adapter.MongoDBHandler;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

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

    private final MongoDBHandler handler = new MongoDBHandler();

    /**
     * The handler accumulates results in an internal list.
     * Clear it after each test to prevent cross-test contamination.
     */
    @AfterEach
    void resetAccumulatingList() {
        handler.setList(new ArrayList<>());
    }

    /**
     * Utility: mocks the full chain
     * MongoClients.create() → MongoClient → MongoDatabase → MongoCollection.
     * Used by all tests to simplify setup.
     */
    private static class Wiring {
        final MockedStatic<MongoClients> staticCreate;
        final MongoClient client = mock(MongoClient.class);
        final MongoDatabase db = mock(MongoDatabase.class);
        final MongoCollection<Document> coll = mock(MongoCollection.class);

        Wiring() {
            staticCreate = mockStatic(MongoClients.class);
            staticCreate.when(() -> MongoClients.create(anyString())).thenReturn(client);
            when(client.getDatabase(anyString())).thenReturn(db);
            when(db.getCollection(anyString())).thenReturn(coll);
        }

        void close() {
            staticCreate.close();
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
    void createUser_insertsOneDocument() {
        Wiring w = new Wiring();
        try {
            handler.createUser("foo", "bar");

            verify(w.coll, times(1)).insertOne(argThat(d ->
                    "bar".equals(d.getString("foo"))));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // retrieveAllData
    // -----------------------------

    /**
     * retrieveAllData(): simulates cursor iteration and verifies
     * that both documents are returned and the client is closed.
     */
    @Test
    void retrieveAllData_returnsAllDocs() {
        Wiring w = new Wiring();
        try {
            @SuppressWarnings("unchecked")
            FindIterable<Document> iterable = mock(FindIterable.class);
            MongoCursor<Document> cursor = mock(MongoCursor.class);

            Document d1 = new Document("a", 1);
            Document d2 = new Document("b", 2);

            when(w.coll.find()).thenReturn(iterable);
            when(iterable.iterator()).thenReturn(cursor);
            when(cursor.hasNext()).thenReturn(true, true, false);
            when(cursor.next()).thenReturn(d1, d2);

            ArrayList<Document> result = handler.retrieveAllData();

            assertEquals(2, result.size());
            assertTrue(result.containsAll(Arrays.asList(d1, d2)));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // retrieveByKeyValue
    // -----------------------------

    /**
     * retrieveByKeyValue(): verifies that find() is called
     * with a correct equality filter {key: value}, and that
     * matching documents are collected and returned.
     */
    @Test
    void retrieveByKeyValue_filtersByEq() {
        Wiring w = new Wiring();
        try {
            @SuppressWarnings("unchecked")
            FindIterable<Document> iterable = mock(FindIterable.class);
            MongoCursor<Document> cursor = mock(MongoCursor.class);

            Document d = new Document("key", "val");

            when(w.coll.find(any(Bson.class))).thenReturn(iterable);
            when(iterable.iterator()).thenReturn(cursor);
            when(cursor.hasNext()).thenReturn(true, false);
            when(cursor.next()).thenReturn(d);

            ArrayList<Document> result = handler.retrieveByKeyValue("key", "val");

            // Verify that Filters.eq("key", "val") was used
            verify(w.coll).find(
                    org.mockito.ArgumentMatchers.<Bson>argThat(b -> {
                        var registry = CodecRegistries.fromRegistries(
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
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // appendData
    // -----------------------------

    /**
     * appendData(): verifies that the correct filter {id:"Hjordis"}
     * and update {$set:{age:"79"}} are built and sent,
     * and that the client is closed.
     */
    @Test
    void appendData_setsAge_forHjordis() {
        Wiring w = new Wiring();
        try {
            when(w.coll.updateOne(any(Bson.class), any(Bson.class)))
                    .thenReturn(mock(UpdateResult.class));

            handler.setIdField("id");
            handler.appendData("Hjordis", "age", "79");

            ArgumentCaptor<Bson> filterCap = ArgumentCaptor.forClass(Bson.class);
            ArgumentCaptor<Bson> updateCap = ArgumentCaptor.forClass(Bson.class);
            verify(w.coll).updateOne(filterCap.capture(), updateCap.capture());

            var registry = CodecRegistries.fromRegistries(
                    com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(new DocumentCodecProvider())
            );
            var filterDoc = filterCap.getValue().toBsonDocument(Document.class, registry);
            var updateDoc = updateCap.getValue().toBsonDocument(Document.class, registry);

            assertEquals("Hjordis", filterDoc.getString("id").getValue());
            var setDoc = updateDoc.getDocument("$set");
            assertNotNull(setDoc);
            assertEquals("79", setDoc.getString("age").getValue());

            verify(w.client).getDatabase(handler.getDbName());
            verify(w.db).getCollection(handler.getCollectionName());
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // removeData (unset)
    // -----------------------------

    /**
     * removeData(userId, key): verifies that the filter is {id:"user-123"}
     * and the update is {$unset:{obsoleteField:""}}.
     */
    @Test
    void removeData_unsetsKey_exactFilterAndUpdate() {
        Wiring w = new Wiring();
        try {
            when(w.coll.updateOne(any(Bson.class), any(Bson.class)))
                    .thenReturn(mock(UpdateResult.class));

            handler.removeData("user-123", "obsoleteField");

            CodecRegistry registry = CodecRegistries.fromRegistries(
                    com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(new DocumentCodecProvider())
            );

            verify(w.coll).updateOne(
                    // Filter: { id: "user-123" }
                    org.mockito.ArgumentMatchers.<Bson>argThat(filter -> {
                        var doc = filter.toBsonDocument(Document.class, registry);
                        return doc != null
                                && doc.getString("id") != null
                                && "user-123".equals(doc.getString("id").getValue());
                    }),
                    // Update: { $unset: { obsoleteField: "" } }
                    org.mockito.ArgumentMatchers.<Bson>argThat(update -> {
                        var doc = update.toBsonDocument(Document.class, registry);
                        var unset = (doc != null) ? doc.getDocument("$unset") : null;
                        return unset != null && unset.containsKey("obsoleteField");
                    })
            );

            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // removeData (delete by id)
    // -----------------------------

    /**
     * removeData(userId): verifies that deleteOne() is called
     * with the correct filter {id:"user-123"}, and that the client is closed.
     */
    @Test
    void removeData_deletesDocById() {
        Wiring w = new Wiring();
        try {
            DeleteResult del = mock(DeleteResult.class);
            when(del.getDeletedCount()).thenReturn(1L);
            when(w.coll.deleteOne(any(Bson.class))).thenReturn(del);

            handler.removeData("user-123");

            verify(w.coll).deleteOne(ArgumentMatchers.<Bson>argThat(b -> {
                var reg = CodecRegistries.fromRegistries(
                        com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(new DocumentCodecProvider()));
                var doc = b.toBsonDocument(Document.class, reg);
                return doc.getString("id") != null
                        && "user-123".equals(doc.getString("id").getValue());
            }));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // deleteManyDocuments
    // -----------------------------

    /**
     * deleteManyDocuments(): verifies that deleteMany() is called
     * with the correct filter {id:"dup-id"}.
     */
    @Test
    void deleteManyDocuments_deletesAllWithSameId() {
        Wiring w = new Wiring();
        try {
            DeleteResult del = mock(DeleteResult.class);
            when(del.getDeletedCount()).thenReturn(3L);
            when(w.coll.deleteMany(any(Bson.class))).thenReturn(del);

            handler.deleteManyDocuments("dup-id");

            verify(w.coll).deleteMany(ArgumentMatchers.<Bson>argThat(b -> {
                var reg = CodecRegistries.fromRegistries(
                        com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(new DocumentCodecProvider()));
                var doc = b.toBsonDocument(Document.class, reg);
                return doc.getString("id") != null
                        && "dup-id".equals(doc.getString("id").getValue());
            }));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // retrieveByValue
    // -----------------------------

    /**
     * retrieveByValue(): verifies that documents containing the search string
     * either as a key or value are returned, others ignored.
     */
    @Test
    void retrieveByValue_matchesOnKeyOrValue() {
        Wiring w = new Wiring();
        try {
            @SuppressWarnings("unchecked")
            FindIterable<Document> iterable = mock(FindIterable.class);
            MongoCursor<Document> cursor = mock(MongoCursor.class);

            Document valueMatch = new Document("field1", "needle");
            Document keyMatch   = new Document("needle", "anything");
            Document noMatch    = new Document("other", "stuff");

            when(w.coll.find()).thenReturn(iterable);
            when(iterable.iterator()).thenReturn(cursor);
            when(cursor.hasNext()).thenReturn(true, true, true, false);
            when(cursor.next()).thenReturn(valueMatch, keyMatch, noMatch);

            var result = handler.retrieveByValue("needle");

            assertEquals(2, result.size());
            assertTrue(result.contains(valueMatch));
            assertTrue(result.contains(keyMatch));

            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // insertDestinationForLiteUser
    // -----------------------------

    /**
     * insertDestinationForLiteUser(): authorized admin + existing favorite updated.
     * Should return true and perform one updateOne() call.
     */
    @Test
    void insertDestinationForLiteUser_updatesExistingFavorite_whenAuthorized() {
        Wiring w = new Wiring();
        try {
            @SuppressWarnings("unchecked")
            FindIterable<Document> adminFind = mock(FindIterable.class);
            when(w.coll.find(any(Bson.class))).thenReturn(adminFind);
            when(adminFind.first()).thenReturn(new Document("id", "admin1"));

            UpdateResult ok = mock(UpdateResult.class);
            when(ok.getModifiedCount()).thenReturn(1L);
            when(w.coll.updateOne(any(Bson.class), any(Bson.class))).thenReturn(ok);

            boolean res = handler.insertDestinationForLiteUser(
                    "lite1", "destA", "Home", "Addr", 59.9, 10.7, "admin1");

            assertTrue(res);
            verify(w.coll, times(1)).updateOne(any(Bson.class), any(Bson.class));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    /**
     * insertDestinationForLiteUser(): authorized admin but destination doesn't exist,
     * should perform two updates (update + push) and return true.
     */
    @Test
    void insertDestinationForLiteUser_pushesNew_whenAuthorizedAndMissing() {
        Wiring w = new Wiring();
        try {
            @SuppressWarnings("unchecked")
            FindIterable<Document> adminFind = mock(FindIterable.class);
            when(w.coll.find(any(Bson.class))).thenReturn(adminFind);
            when(adminFind.first()).thenReturn(new Document("id", "admin1"));

            UpdateResult none = mock(UpdateResult.class);
            when(none.getModifiedCount()).thenReturn(0L);
            UpdateResult pushed = mock(UpdateResult.class);
            when(pushed.getModifiedCount()).thenReturn(1L);

            when(w.coll.updateOne(any(Bson.class), any(Bson.class)))
                    .thenReturn(none)
                    .thenReturn(pushed);

            boolean res = handler.insertDestinationForLiteUser(
                    "lite1", "destB", "Work", "Addr2", 60.0, 11.0, "admin1");

            assertTrue(res);
            verify(w.coll, times(2)).updateOne(any(Bson.class), any(Bson.class));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    /**
     * insertDestinationForLiteUser(): unauthorized admin.
     * Should return false and perform no updates.
     */
    @Test
    void insertDestinationForLiteUser_returnsFalse_whenUnauthorized() {
        Wiring w = new Wiring();
        try {
            @SuppressWarnings("unchecked")
            FindIterable<Document> adminFind = mock(FindIterable.class);
            when(w.coll.find(any(Bson.class))).thenReturn(adminFind);
            when(adminFind.first()).thenReturn(null);

            boolean res = handler.insertDestinationForLiteUser(
                    "lite1", "destX", "Gym", "Addr3", 59.0, 10.0, "adminX");

            assertFalse(res);
            verify(w.coll, never()).updateOne(any(Bson.class), any(Bson.class));
            verify(w.client).close();
        } finally {
            w.close();
        }
    }

    // -----------------------------
    // Exception handling
    // -----------------------------

    /**
     * Smoke test for exception handling: if MongoClient.getDatabase() throws,
     * each method should catch the exception and not rethrow.
     */
    @Test
    void methods_handleMongoException_gracefully() {
        Wiring w = new Wiring();
        try {
            when(w.client.getDatabase(anyString())).thenThrow(new MongoException("error"));

            assertDoesNotThrow(() -> handler.createUser("x", "y"));
            assertDoesNotThrow(() -> handler.retrieveAllData());
            assertDoesNotThrow(() -> handler.retrieveByKeyValue("k", "v"));
            assertDoesNotThrow(() -> handler.appendData("id", "k", "v"));
            assertDoesNotThrow(() -> handler.removeData("id", "k"));
            assertDoesNotThrow(() -> handler.insertDestinationForLiteUser(
                    "lite", "d", "n", "a", 1.0, 2.0, "admin"));

            verify(w.client, atLeastOnce()).close();
        } finally {
            w.close();
        }
    }
}
