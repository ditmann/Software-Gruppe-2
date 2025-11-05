package avandra.test;

import avandra.core.port.DBConnectionPort;
import avandra.storage.adapter.MongoDBConnectionAdapter;
import avandra.storage.adapter.MongoDBHandlerAdapter;
import org.bson.Document;
import org.junit.jupiter.api.*;

import org.testcontainers.containers.MongoDBContainer;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MongoDBHandlerAdapterIT {

    static MongoDBContainer mongo;
    static MongoDBHandlerAdapter handler;

    @BeforeAll
    static void setup() throws Exception {
        // Spin up a real MongoDB instance in Docker using Testcontainers.
        // This gives us an actual running database for the integration test
        mongo = new MongoDBContainer("mongo:7.0");
        mongo.start();

        // Create a real DBConnectionPort that points at the Mongo container.
        DBConnectionPort realConn = new MongoDBConnectionAdapter(
                mongo.getConnectionString(), // connection URI from the container
                "testdb",                    // test database name (can be anything)
                "users"                      // collection name we want to use
        );

        // Create the real handler we're testing, but now backed by the container DB.
        handler = new MongoDBHandlerAdapter(realConn);

        // Make sure the handler is using the same "id" field we rely on in production.
        // (If MongoDBHandlerAdapter defaults to some other key, set it here.)
        handler.setIdField("id");
    }

    @AfterAll
    static void teardown() {
        // Stop the Mongo container after all tests are done.
        mongo.stop();
    }

    @Test
    void createUser_appendData_readBack_roundtrip() throws Exception {
        // createUser should insert a new user document for "Per".
        handler.createUser("Per", true);

        // appendData should update that same user with an extra field.
        handler.appendData("Per", "age", "78");

        // This is how production code would read all user documents back out.
        ArrayList<Document> all = handler.retrieveAllData();

        // We expect at least one user to exist in the DB now.
        assertFalse(all.isEmpty(), "expected at least one user in DB");

        // Find the document we just created for Per.
        Document per = all.stream()
                .filter(doc -> "Per".equals(doc.getString("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Per not found in DB"));

        // Verify that createUser added the base fields we expect for a new user.
        assertEquals("Per", per.getString("id"));
        assertTrue(per.containsKey("admin"));
        assertTrue(per.containsKey("litebrukere"));
        assertTrue(per.containsKey("planlagte reiser"));
        assertTrue(per.containsKey("favoritter"));

        // Verify that appendData actually persisted "age" = "78" in Mongo,
        // not just in memory.
        assertEquals("78", per.get("age"));

        // retrieveByKeyValue("id", "Per") should also be able to find this user.
        var byKeyVal = handler.retrieveByKeyValue("id", "Per");
        assertFalse(byKeyVal.isEmpty(), "retrieveByKeyValue should find Per");
    }

    @Test
    void deleteUser_removesDocument() throws Exception {
        handler.createUser("Petter", false);

        handler.setList(new ArrayList<>()); // clear cache before reading
        var before = handler.retrieveByKeyValue("id", "Petter");
        assertFalse(before.isEmpty(), "user Petter should exist before delete");

        handler.removeData("Petter");

        handler.setList(new ArrayList<>()); // clear cache before reading again
        var after = handler.retrieveByKeyValue("id", "Petter");
        assertTrue(after.isEmpty(), "user Petter should be gone after delete");
    }

}
