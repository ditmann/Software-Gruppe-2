package avandra.test;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.DTO.TripPartDTO;
import avandra.core.port.EnturClientPort;
import avandra.core.service.TripFileHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * tests for  TripFileHandler
 *
 * - takes a List<CoordinateDTO> [from, to]
 * - calls Entur with the right args
 * - writes Trip.json with {request, trip} when includeRequestMeta = true
 * - returns a matrix of legs where each inner list is one tripPattern
 */
@ExtendWith(MockitoExtension.class)
class TripFileHandlerTest {

    @Mock EnturClientPort entur;

    ObjectMapper mapper;
    TripFileHandler handler;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new TripFileHandler(entur, mapper);
        // clean up old test file if it exists
        new File("Trip.json").delete();
    }

    @AfterEach
    void tearDown() {
        // remove the test file after each run
        new File("Trip.json").delete();
    }

    // small helper that behaves like our CoordinateDTO class
    private static final class TestCoord extends CoordinateDTO {
        private final double lat, lon;
        TestCoord(double lat, double lon) { this.lat = lat; this.lon = lon; }
         public double getLatitudeNum() { return lat; }
         public double getLongitudeNUM() { return lon; }
    }

    // minimal fake Entur trip payload
    // one pattern with a single leg to keep parsing simple
    private JsonNode sampleTrip(ObjectMapper m) {
        ObjectNode trip = m.createObjectNode();
        var legs = trip.putArray("tripPatterns")
                .addObject()
                .putArray("legs");
        legs.addObject()
                .put("mode", "bus")
                .put("distance", 740)
                .set("line", m.createObjectNode()
                        .put("id", "RUT:Line:25")
                        .put("name", "Bekkestua - Majorstuen")
                        .put("publicCode", "25")
                        .put("transportMode", "bus")
                        .set("authority", m.createObjectNode().put("name", "Ruter")));
        return trip;
    }

    @Test
    void planTrip_writesFileWithRequest_andReturnsGroupedMatrix() throws Exception {
        // set up a simple from/to and pattern count
        var from = new TestCoord(59.91, 10.75);
        var to   = new TestCoord(63.43, 10.39);
        int n = 3;

        // fake Entur returning a known payload
        when(entur.planTripCoords(from.getLatitudeNum(), from.getLongitudeNUM(),
                to.getLatitudeNum(),   to.getLongitudeNUM(), n))
                .thenReturn(sampleTrip(mapper));

        // run the method
        List<CoordinateDTO> coords = List.of(from, to);
        List<List<TripPartDTO>> matrix = handler.planTrip(coords, n, true);

        // we should get a matrix back with one pattern and one leg inside
        assertNotNull(matrix, "handler should return a matrix");
        assertEquals(1, matrix.size(), "expected one pattern in the test data");
        assertEquals(1, matrix.get(0).size(), "expected one leg inside the pattern");

        TripPartDTO leg = matrix.get(0).get(0);
        assertEquals("bus", leg.getLegTransportMode(), "mode should match JSON");
        assertEquals(740, leg.getTravelDistance(), "distance should match JSON");
        assertEquals("RUT:Line:25", leg.getLineId(), "line id should match JSON");
        assertEquals("25", leg.getLineNumber(), "line number should match JSON");

        // check that Trip.json exists and has both request + trip
        File out = new File("Trip.json");
        assertTrue(out.exists(), "Trip.json should be written");

        JsonNode root = mapper.readTree(out);
        assertTrue(root.has("request"), "file should have 'request' when includeRequestMeta = true");
        assertTrue(root.has("trip"), "file should have 'trip' when includeRequestMeta = true");

        JsonNode req = root.get("request");
        assertEquals(from.getLatitudeNum(),  req.get("fromLat").asDouble(),  1e-9);
        assertEquals(from.getLongitudeNUM(), req.get("fromLon").asDouble(),  1e-9);
        assertEquals(to.getLatitudeNum(),    req.get("toLat").asDouble(),    1e-9);
        assertEquals(to.getLongitudeNUM(),   req.get("toLon").asDouble(),    1e-9);
        assertEquals(n,                      req.get("numPatterns").asInt());

        // make sure Entur got called once with the exact coords
        verify(entur, times(1)).planTripCoords(
                from.getLatitudeNum(), from.getLongitudeNUM(),
                to.getLatitudeNum(),   to.getLongitudeNUM(),
                n
        );
    }

    @Test
    void planTrip_errorsOnBadInput_andPropagatesIOException() throws Exception {
        // null list should throw
        assertThrows(IllegalArgumentException.class, () -> handler.planTrip(null, 1, false));

        // list with only one coord should also throw
        assertThrows(IllegalArgumentException.class,
                () -> handler.planTrip(List.of(new TestCoord(59.91, 10.75)), 1, false));

        // if Entur throws IOException
        when(entur.planTripCoords(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenThrow(new IOException("simulated failure"));

        var from = new TestCoord(59.91, 10.75);
        var to   = new TestCoord(63.43, 10.39);

        assertThrows(IOException.class, () -> handler.planTrip(List.of(from, to), 2, true));
    }
}
