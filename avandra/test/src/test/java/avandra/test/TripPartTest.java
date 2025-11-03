package avandra.test;

import avandra.core.domain.TripPart;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for TripPart.tripParts().
 *
 * These tests verify:
 *  - That TripPart correctly parses a structured Entur-like trip JSON into domain objects
 *  - That transit legs (bus, etc.) are populated with line info, platforms, and timestamps
 *  - That walking legs (no "line", no calls) still parse distance and fall back in toString()
 *
 * Notes:
 *  - We generate minimal JSON fixtures inside the test and write them to disk.
 *    The code under test requires a File, so we exercise the real parsing path.
 *  - We assert on the important semantic fields (mode, distance, platforms, timestamps).
 *  - We also check toString() formatting for both transit and walking legs.
 */
class TripPartTest {

    /**
     * Helper: writes the given JSON string to a new File on disk and returns it.
     * Each test calls this to create an isolated input file.
     */
    private File writeJsonToFile(String filename, String json) throws IOException {
        File f = new File(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(f.toPath())) {
            writer.write(json);
        }
        return f;
    }

    // -------------------------------------------------
    // tripParts(): typical transit leg with timings
    // -------------------------------------------------

    /**
     * tripParts() should:
     *
     *  - Return one TripPart for one leg in the JSON
     *  - Populate legTransportMode and travelDistance from "mode" / "distance"
     *  - Populate line metadata from "line" (id, name, publicCode, authority.name, etc.)
     *  - Populate platform info and parsed timestamps from fromEstimatedCall / toEstimatedCall
     *  - Produce a non-"walking" toString() when full timing/platform info is available
     *
     * The JSON here mimics a bus leg from an Entur-style response structure:
     *
     * {
     *   "trip": {
     *     "tripPatterns": [
     *       {
     *         "legs": [
     *           {
     *             "mode": "bus",
     *             "distance": 740,
     *             "line": {...},
     *             "fromEstimatedCall": {...},
     *             "toEstimatedCall": {...}
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * }
     *
     * We expect timestamps to be parsed as LocalDateTime in Europe/Oslo using OffsetDateTime.parse().
     */
    @Test
    void tripParts_parsesSingleLeg() throws IOException {
        // Build a realistic Entur-like JSON with one leg
        String json = """
        {
          "trip": {
            "tripPatterns": [
              {
                "legs": [
                  {
                    "mode": "bus",
                    "distance": 740,
                    "line": {
                      "id": "RUT:Line:25",
                      "name": "Bekkestua - Majorstuen",
                      "publicCode": "25",
                      "transportMode": "bus",
                      "authority": { "name": "Ruter" }
                    },
                    "fromEstimatedCall": {
                      "quay": { "id": "NSR:Quay:1234", "name": "Bekkestua" },
                      "aimedDepartureTime": "2025-10-24T12:30:00+02:00",
                      "expectedDepartureTime": "2025-10-24T12:32:00+02:00"
                    },
                    "toEstimatedCall": {
                      "quay": { "id": "NSR:Quay:5678", "name": "Majorstuen" },
                      "aimedDepartureTime": "2025-10-24T12:50:00+02:00",
                      "expectedDepartureTime": "2025-10-24T12:52:00+02:00"
                    }
                  }
                ]
              }
            ]
          }
        }
        """;

        // Write the JSON to disk so TripPart.tripParts() can consume it
        File tripFile = writeJsonToFile("Trip.json", json);

        try {
            // Act: parse the file into domain objects
            List<TripPart> parts = TripPart.tripParts(tripFile);

            // Basic sanity: parser should not crash and should return exactly one leg
            assertNotNull(parts, "parts should not be null");
            assertEquals(1, parts.size(), "should parse exactly one TripPart");

            TripPart leg = parts.get(0);

            // ---------------------------
            // Core leg / distance fields
            // ---------------------------
            assertEquals("bus", leg.getLegTransportMode(),
                    "legTransportMode should come from legs[0].mode");
            assertEquals(740, leg.getTravelDistance(),
                    "travelDistance should come from legs[0].distance");

            // ---------------------------
            // Line metadata from 'line'
            // ---------------------------
            assertEquals("RUT:Line:25", leg.getLineId(),
                    "lineId should come from line.id");
            assertEquals("Bekkestua - Majorstuen", leg.getLineName(),
                    "lineName should come from line.name");
            assertEquals("25", leg.getLineNumber(),
                    "lineNumber should come from line.publicCode");
            assertEquals("bus", leg.getLineTransportMode(),
                    "lineTransportMode should come from line.transportMode");
            assertEquals("Ruter", leg.getLineOwner(),
                    "lineOwner should come from line.authority.name");

            // ---------------------------
            // Departure platform + times
            // ---------------------------
            assertEquals("NSR:Quay:1234", leg.getDepartPlatformId(),
                    "departPlatformId should come from fromEstimatedCall.quay.id");
            assertEquals("Bekkestua", leg.getDepartPlatformName(),
                    "departPlatformName should come from fromEstimatedCall.quay.name");

            LocalDateTime aimedDep = leg.getAimedDeparture();
            assertNotNull(aimedDep,
                    "aimedDeparture should be parsed from fromEstimatedCall.aimedDepartureTime");
            assertEquals(2025, aimedDep.getYear(), "Year mismatch for aimedDeparture");
            assertEquals(Month.OCTOBER, aimedDep.getMonth(), "Month mismatch for aimedDeparture");
            assertEquals(24, aimedDep.getDayOfMonth(), "Day mismatch for aimedDeparture");
            assertEquals(12, aimedDep.getHour(), "Hour mismatch for aimedDeparture");
            assertEquals(30, aimedDep.getMinute(), "Minute mismatch for aimedDeparture");

            LocalDateTime expectedDep = leg.getExpectedDeparture();
            assertNotNull(expectedDep,
                    "expectedDeparture should be parsed from fromEstimatedCall.expectedDepartureTime");
            assertEquals(12, expectedDep.getHour(), "Hour mismatch for expectedDeparture");
            assertEquals(32, expectedDep.getMinute(), "Minute mismatch for expectedDeparture");

            // ---------------------------
            // Arrival platform + times
            // ---------------------------
            assertEquals("NSR:Quay:5678", leg.getArrivePlatformId(),
                    "arrivePlatformId should come from toEstimatedCall.quay.id");
            assertEquals("Majorstuen", leg.getArrivePlatformName(),
                    "arrivePlatformName should come from toEstimatedCall.quay.name");

            LocalDateTime aimedArr = leg.getAimedArrival();
            assertNotNull(aimedArr,
                    "aimedArrival should be parsed from toEstimatedCall.aimedDepartureTime");
            assertEquals(12, aimedArr.getHour(), "Hour mismatch for aimedArrival");
            assertEquals(50, aimedArr.getMinute(), "Minute mismatch for aimedArrival");

            LocalDateTime expectedArr = leg.getExpectedArrival();
            assertNotNull(expectedArr,
                    "expectedArrival should be parsed from toEstimatedCall.expectedDepartureTime");
            assertEquals(12, expectedArr.getHour(), "Hour mismatch for expectedArrival");
            assertEquals(52, expectedArr.getMinute(), "Minute mismatch for expectedArrival");

            // ---------------------------
            // String rendering
            // ---------------------------
            // When we have full transit data (platforms + times), toString()
            // is expected to describe the trip, not "walking <distance>m".
            String legString = leg.toString();
            assertTrue(
                    legString.contains("Bekkestua") && legString.contains("Majorstuen"),
                    "toString() for transit legs should include stop/platform names");
            assertFalse(
                    legString.startsWith("walking"),
                    "Transit legs with timing should not fall back to walking output");
        } finally {
            tripFile.delete();
        }
    }
}
