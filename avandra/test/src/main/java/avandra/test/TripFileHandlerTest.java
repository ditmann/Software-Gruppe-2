package avandra.test;

import avandra.core.port.EnturClient;
import avandra.storage.adapter.TripFileHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TripFileHandler.
 *
 * Verifies:
 *  - Delegation to EnturClient with correct arguments
 *  - JSON structure written to disk with and without request metadata
 *  - Propagation of IO exceptions from the EnturClient call
 *  - Basic file lifecycle (created/cleaned up)
 */
@ExtendWith(MockitoExtension.class)
class TripFileHandlerTest {

    @Mock
    // Mocked dependency the handler uses to fetch trip data
    EnturClient entur;
    // Shared JSON mapper for constructing/reading test payloads
    ObjectMapper mapper;
    // System under test
    TripFileHandler handler;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new TripFileHandler(entur, mapper);

        // Ensure we start fresh: remove previous file if it exists
        new File("Trip.json").delete();
    }

    @AfterEach
    void tearDown() {
        // Clean up the file created by tests
        new File("Trip.json").delete();
    }

    /**
     * Helper: returns a minimal, valid trip JSON structure that looks like Entur’s response.
     */
    private JsonNode sampleTrip() {
        ObjectNode trip = mapper.createObjectNode();
        trip.putArray("tripPatterns")
                .addObject()
                .put("startTime", 123456789L)
                .put("duration", 900)
                .put("walkDistance", 1200.5);
        return trip;
    }

    /**
     * When includeRequestMeta=false:
     *  - The handler should write the trip JSON as returned by Entur (no wrapper).
     *  - The file should exist and not contain a "request" field.
     */
    @Test
    void writesTripOnly_whenIncludeRequestMetaFalse() throws Exception {
        double a=59.91,b=10.75,c=63.43,d=10.39; int n=3;

        // Mock Entur returning a known trip payload
        when(entur.planTripCoords(a,b,c,d,n)).thenReturn(sampleTrip());

        // Execute: write the file without request metadata
        File out = handler.planTripCoordsToFile(a,b,c,d,n,false);

        // Assert: file exists and contains only the trip content
        assertTrue(out.exists());
        JsonNode written = mapper.readTree(out);
        assertTrue(written.has("tripPatterns"));
        assertFalse(written.has("request"));

        // Verify the handler called Entur with the expected arguments once
        verify(entur).planTripCoords(a,b,c,d,n);
    }

    /**
     * When includeRequestMeta=true:
     *  - The handler should write an object with "request" and "trip" fields.
     *  - "request" must match the input parameters.
     *  - "trip" must contain the Entur trip payload.
     */
    @Test
    void writesRequestAndTrip_whenIncludeRequestMetaTrue() throws Exception {
        double a=59.91,b=10.75,c=63.43,d=10.39; int n=3;

        // Mock Entur returning a known trip payload
        when(entur.planTripCoords(a,b,c,d,n)).thenReturn(sampleTrip());

        // Execute: write the file including request metadata
        File out = handler.planTripCoordsToFile(a,b,c,d,n,true);

        // Parse written JSON and assert shape/content
        JsonNode root = mapper.readTree(out);
        assertTrue(root.has("request"));
        assertTrue(root.has("trip"));

        // Build expected "request" JSON and compare exactly
        ObjectNode expectedReq = mapper.createObjectNode()
                .put("fromLat", a)
                .put("fromLon", b)
                .put("toLat",   c)
                .put("toLon",   d)
                .put("numPatterns", n);

        ObjectNode req = (ObjectNode) root.get("request");
        assertEquals(expectedReq, req);

        // Ensure the trip payload is present
        assertTrue(root.get("trip").has("tripPatterns"));

        // Verify Entur was called once with the expected arguments
        verify(entur).planTripCoords(a,b,c,d,n);
    }

    /**
     * Propagates IOException thrown by EnturClient:
     *  - If Entur throws IOException, the handler should rethrow it to the caller.
     */
    @Test
    void throwingIOException() throws Exception {
        // Arrange: Entur throws an IOException for any call
        when(entur.planTripCoords(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenThrow(new IOException("simulated error"));

        // Assert: handler rethrows IOException
        assertThrows(IOException.class, () ->
                handler.planTripCoordsToFile(1,2,3,4,1,false));
    }
}
