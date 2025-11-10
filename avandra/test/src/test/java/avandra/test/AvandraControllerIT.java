package avandra.test;

import avandra.Controllers.AvandraController;
import avandra.core.DTO.CoordinateDTO;
import avandra.core.DTO.TripPartDTO;
import avandra.core.port.DBConnectionPort;
import avandra.core.port.EnturClientPort;
import avandra.core.port.LocationPort;
import avandra.core.service.DBService;
import avandra.core.service.FindBestTripService;
import avandra.core.service.JourneyPlannerService;
import avandra.storage.adapter.MongoDBConnectionAdapter;
import avandra.storage.adapter.MongoDBHandlerAdapter;
import avandra.core.service.TripFileHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class AvandraControllerIT {

    // real mongodb so we test storage flow end to end
    @Container
    static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");

    private DBService dbService;
    private AvandraController controller;

    // simple fakes for external ports so test stays deterministic and offline
    private static final class FakeLocationPort implements LocationPort {
        @Override public CoordinateDTO currentCoordinate() {
            return new CoordinateDTO(59.9100, 10.7500);
        }
    }

    private static final class FakeEnturClient implements EnturClientPort {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public JsonNode planTripCoords(double fromLat, double fromLon, double toLat, double toLon, int numPatterns) {
            try {
                // two alternatives where the second is better for our simple scoring
                String jsonResponse = """
                {
                  "tripPatterns": [
                    {
                      "legs": [
                        {
                          "mode": "foot",
                          "distance": 400,
                          "fromEstimatedCall": {
                            "quay": { "id": "Q1", "name": "WalkStart" },
                            "expectedDepartureTime": "2025-01-01T10:00:00Z"
                          },
                          "toEstimatedCall": {
                            "quay": { "id": "Q2", "name": "WalkEnd" },
                            "expectedDepartureTime": "2025-01-01T10:05:00Z"
                          }
                        },
                        {
                          "mode": "bus",
                          "distance": 0,
                          "line": {
                            "id": "BUS-10",
                            "name": "Bus 10",
                            "publicCode": "10",
                            "transportMode": "bus",
                            "authority": { "name": "Ruter" }
                          },
                          "fromEstimatedCall": {
                            "quay": { "id": "QB1", "name": "Stop A" },
                            "expectedDepartureTime": "2025-01-01T10:06:00Z"
                          },
                          "toEstimatedCall": {
                            "quay": { "id": "QB2", "name": "Stop B" },
                            "expectedDepartureTime": "2025-01-01T10:30:00Z"
                          }
                        }
                      ]
                    },
                    {
                      "legs": [
                        {
                          "mode": "foot",
                          "distance": 200,
                          "fromEstimatedCall": {
                            "quay": { "id": "Q3", "name": "WalkStart2" },
                            "expectedDepartureTime": "2025-01-01T10:00:00Z"
                          },
                          "toEstimatedCall": {
                            "quay": { "id": "Q4", "name": "WalkEnd2" },
                            "expectedDepartureTime": "2025-01-01T10:02:00Z"
                          }
                        },
                        {
                          "mode": "tram",
                          "distance": 0,
                          "line": {
                            "id": "TR-1",
                            "name": "Tram 1",
                            "publicCode": "T1",
                            "transportMode": "tram",
                            "authority": { "name": "Ruter" }
                          },
                          "fromEstimatedCall": {
                            "quay": { "id": "QT1", "name": "Tram A" },
                            "expectedDepartureTime": "2025-01-01T10:03:00Z"
                          },
                          "toEstimatedCall": {
                            "quay": { "id": "QT2", "name": "Tram B" },
                            "expectedDepartureTime": "2025-01-01T10:20:00Z"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
                return objectMapper.readTree(jsonResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        String databaseName = "itdb_" + UUID.randomUUID();
        String collectionName = "users";

        DBConnectionPort dbConnection =
                new MongoDBConnectionAdapter(mongoContainer.getConnectionString(), databaseName, collectionName);

        MongoDBHandlerAdapter handler = new MongoDBHandlerAdapter(dbConnection);
        handler.setIdField("id");
        dbService = new DBService(handler);

        TripFileHandler tripFileHandler = new TripFileHandler(new FakeEnturClient(), new ObjectMapper());
        FindBestTripService bestTripService = new FindBestTripService();
        JourneyPlannerService journeyPlannerService = new JourneyPlannerService(new FakeLocationPort(), dbService);

        controller = new AvandraController(dbService, tripFileHandler, bestTripService, journeyPlannerService);
    }

    // helper to read back a user document from the database
    @SuppressWarnings("unchecked")
    private Optional<Document> findUserById(String userId) {
        ArrayList<Document> allDocs = (ArrayList<Document>) dbService.retriveALLData();
        return allDocs.stream().filter(d -> userId.equals(d.getString("id"))).findFirst();
    }

    @Test
    void bestJourney_returnsBestAlternative_forUserAndDestination() throws Exception {
        dbService.createUser("userA", false);
        dbService.addDestinationToFavorites("userA", "Office", "Work Ave 2", 59.9200, 10.7600);

        List<TripPartDTO> chosenTrip = controller.bestJourney("userA", "Office");

        assertNotNull(chosenTrip);
        assertEquals(2, chosenTrip.size());

        TripPartDTO firstLeg = chosenTrip.get(0);
        TripPartDTO secondLeg = chosenTrip.get(1);

        assertEquals("foot", firstLeg.getLegTransportMode().toLowerCase());
        assertEquals(200, firstLeg.getTravelDistance());

        assertEquals("tram", secondLeg.getLegTransportMode().toLowerCase());
        assertEquals("TR-1", secondLeg.getLineId());
    }

    @Test
    void adminAddUpdateRemoveFavorite_andListLiteUserDestinations() throws Exception {
        dbService.createUser("adminUser", true);
        dbService.createUser("liteUser", false);
        dbService.appendData("adminUser", "litebrukere", List.of("liteUser"));

        controller.adminAddFavorite("adminUser", "", "Home", "Main St 1", 59.91, 10.75);
        controller.adminAddFavorite("adminUser", "liteUser", "School", "Elm St 2", 59.95, 10.80);

        Document adminDoc = findUserById("adminUser").orElseThrow();
        Document adminFavorites = adminDoc.get("favoritter", Document.class);
        assertTrue(adminFavorites.containsKey("Home"));

        Document liteDoc = findUserById("liteUser").orElseThrow();
        Document liteFavorites = liteDoc.get("favoritter", Document.class);
        assertTrue(liteFavorites.containsKey("School"));

        controller.adminUpdateFavoriteCoordinates("adminUser", "liteUser", "School", 60.0, 11.0);
        var schoolCoords = dbService.searchDestination("liteUser", "School");
        assertNotNull(schoolCoords);
        assertEquals(60.0, schoolCoords.getLatitudeNum(), 1e-6);
        assertEquals(11.0, schoolCoords.getLongitudeNUM(), 1e-6);

        controller.adminRemoveFavorite("adminUser", "liteUser", "School");
        List<String> remainingLiteDestinations = controller.adminListLiteUserDestinations("adminUser", "liteUser");
        assertTrue(remainingLiteDestinations.isEmpty());
    }

    @Test
    void bestJourney_validatesInputIsNotBlank() {
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney(null, "d"));
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney("", "d"));
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney("u", null));
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney("u", "  "));
    }
}
