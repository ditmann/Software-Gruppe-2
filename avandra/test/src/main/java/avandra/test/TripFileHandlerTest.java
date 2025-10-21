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

@ExtendWith(MockitoExtension.class)
class TripFileHandlerTest {

    @Mock
    EnturClient entur;

    ObjectMapper mapper;
    TripFileHandler handler;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new TripFileHandler(entur, mapper);
        new File("Trip.json").delete();
    }

    @AfterEach
    void tearDown() {
        new File("Trip.json").delete();
    }

    private JsonNode sampleTrip() {
        ObjectNode trip = mapper.createObjectNode();
        trip.putArray("tripPatterns")
                .addObject()
                .put("startTime", 123456789L)
                .put("duration", 900)
                .put("walkDistance", 1200.5);
        return trip;
    }

    @Test
    void writesTripOnly_whenIncludeRequestMetaFalse() throws Exception {
        double a=59.91,b=10.75,c=63.43,d=10.39; int n=3;
        when(entur.planTripCoords(a,b,c,d,n)).thenReturn(sampleTrip());

        File out = handler.planTripCoordsToFile(a,b,c,d,n,false);

        assertTrue(out.exists());
        JsonNode written = mapper.readTree(out);
        assertTrue(written.has("tripPatterns"));
        assertFalse(written.has("request"));
        verify(entur).planTripCoords(a,b,c,d,n);
    }

    @Test
    void writesRequestAndTrip_whenIncludeRequestMetaTrue() throws Exception {
        double a=59.91,b=10.75,c=63.43,d=10.39; int n=3;
        when(entur.planTripCoords(a,b,c,d,n)).thenReturn(sampleTrip());

        File out = handler.planTripCoordsToFile(a,b,c,d,n,true);

        JsonNode root = mapper.readTree(out);
        assertTrue(root.has("request"));
        assertTrue(root.has("trip"));
        ObjectNode req = (ObjectNode) root.get("request");

        ObjectNode expectedReq = mapper.createObjectNode()
                .put("fromLat", a)
                .put("fromLon", b)
                .put("toLat",   c)
                .put("toLon",   d)
                .put("numPatterns", n);
        assertEquals(expectedReq, req);

        assertTrue(root.get("trip").has("tripPatterns"));
        verify(entur).planTripCoords(a,b,c,d,n);
    }

    @Test
    void throwingIOException() throws Exception {
        when(entur.planTripCoords(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenThrow(new IOException("simulated error"));

        assertThrows(IOException.class, () ->
                handler.planTripCoordsToFile(1,2,3,4,1,false));
    }
}
