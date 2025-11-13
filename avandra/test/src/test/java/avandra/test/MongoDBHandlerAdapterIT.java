package avandra.test;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.DBConnectionPort;
import avandra.core.service.DBService;
import avandra.storage.adapter.MongoDBConnectionAdapter;
import avandra.storage.adapter.MongoDBHandlerAdapter;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Integration tests for the MongoDB adapter with Testcontainers
 * Runs against a real MongoDB to check the full CRUD flow
 * Verifies that users and destinations are stored and read correctly
 * Makes sure data is cleaned up between test runs
 * Confirms that the persistence layer behaves the same as in production
 */

@Testcontainers
class MongoDBHandlerAdapterIT {

    @Container
    static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");

    private DBService dbService;
    private MongoDBHandlerAdapter dbHandler;

    @BeforeEach
    void setUp() throws Exception {
        String databaseName = "itdb_" + UUID.randomUUID();
        String collectionName = "users";

        DBConnectionPort connection =
                new MongoDBConnectionAdapter(mongoContainer.getConnectionString(), databaseName, collectionName);

        dbHandler = new MongoDBHandlerAdapter(connection);
        dbHandler.setIdField("id");
        dbService = new DBService(dbHandler);
    }

    // --- small helpers -------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Optional<Document> getUserDoc(String userId) {
        ArrayList<Document> all = (ArrayList<Document>) dbService.retriveALLData();
        return all.stream().filter(d -> userId.equals(d.getString("id"))).findFirst();
    }

    // --- tests ---------------------------------------------------------------

    @Test
    void createUser_appendData_and_readBack() {
        String userId = "u1";

        dbService.createUser(userId, true);
        dbService.appendData(userId, "age", 30);

        Document u1 = getUserDoc(userId).orElseThrow();
        assertEquals(userId, u1.getString("id"));
        assertTrue(u1.containsKey("admin"));
        assertTrue(u1.containsKey("litebrukere"));
        assertTrue(u1.containsKey("planlagte reiser"));
        assertTrue(u1.containsKey("favoritter"));
        assertEquals(30, u1.get("age"));

        // exercise retrieveByKeyValue / retrieveByValue on real data
        ArrayList<Document> byKey = dbHandler.retrieveByKeyValue("id", userId);
        assertEquals(1, byKey.size());
        ArrayList<Document> byValue = dbHandler.retrieveByValue("age");
        assertTrue(byValue.stream().anyMatch(d -> userId.equals(d.getString("id"))));
    }

    @Test
    void favorites_add_list_and_update_coordinates() {
        String userId = "u2";
        String destName = "Home";

        dbService.createUser(userId, false);
        dbService.addDestinationToFavorites(userId, destName, "Main St 1", 59.91, 10.75);

        List<String> names = dbService.listUserDestinations(userId);
        assertEquals(List.of(destName), names);

        CoordinateDTO coords = dbService.searchDestination(userId, destName);
        assertNotNull(coords);
        assertEquals(59.91, coords.getLatitudeNum(), 1e-6);
        assertEquals(10.75, coords.getLongitudeNUM(), 1e-6);

        dbService.addCoordinatesToDestination(userId, destName, 59.92, 10.76);
        CoordinateDTO updated = dbService.searchDestination(userId, destName);
        assertNotNull(updated);
        assertEquals(59.92, updated.getLatitudeNum(), 1e-6);
        assertEquals(10.76, updated.getLongitudeNUM(), 1e-6);
    }

    @Test
    void litebrukere_list_and_admin_flag() {
        String adminId = "adminA";

        dbService.createUser(adminId, true);
        dbService.createUser("kid-1", false);
        dbService.createUser("kid-2", false);

        // store a mixed list (includes blank) to match unit test behavior
        dbService.appendData(adminId, "litebrukere", List.of("kid-1", " ", "kid-2"));

        List<String> lite = dbService.listLitebrukereForAdmin(adminId);
        assertEquals(List.of("kid-1", "kid-2"), lite);

        assertTrue(dbService.isAdmin(adminId));
        dbService.createUser("notAdmin", false);
        assertFalse(dbService.isAdmin("notAdmin"));
    }

    @Test
    void remove_field_and_delete_user_and_deleteMany() {
        String userId = "petter";

        dbService.createUser(userId, false);
        dbService.appendData(userId, "age", 30);

        // unset a field
        dbService.removeData(userId, "age");
        Document afterUnset = getUserDoc(userId).orElseThrow();
        assertFalse(afterUnset.containsKey("age"));

        // delete the doc
        dbService.removeData(userId);
        assertTrue(getUserDoc(userId).isEmpty());

        // create again then deleteMany (branch coverage)
        dbService.createUser(userId, false);
        dbHandler.deleteManyDocuments(userId);
        assertTrue(getUserDoc(userId).isEmpty());
    }

    @Test
    void searchFavDestination_returnsNull_when_nodes_missing() {
        String userId = "u3";
        dbService.createUser(userId, false);

        // no favorites yet
        assertNull(dbService.searchDestination(userId, "Home"));

        // add a destination without coords and expect null
        dbHandler.appendData(userId, "favoritter", new Document("Home", new Document()));
        assertNull(dbService.searchDestination(userId, "Home"));
    }
}
