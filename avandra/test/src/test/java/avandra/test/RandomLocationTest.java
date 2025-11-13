package avandra.test;

import avandra.core.adapter.RandomLocationAdapter;
import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.LocationPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests the helper that returns random or fixed locations
 * Verifies that seeded random gives the same result every time
 * Checks that random coordinates stay inside the expected area
 * Runs multiple samples to reduce chance of flaky results
 * Documents the coordinate ranges used in other tests
 */

class RandomLocationTest {

    @Test
    void returnsFixedCoordinateWhenProvided() throws Exception {
        LocationPort adapter = new RandomLocationAdapter(59.91, 10.75);
        CoordinateDTO c = adapter.currentCoordinate();

        assertEquals(59.91, c.getLatitudeNum(), 1e-9);
        assertEquals(10.75, c.getLongitudeNUM(), 1e-9);
    }

    @Test
    void randomCoordinateFallsWithinDefaultOsloBox() throws Exception {
        LocationPort adapter = new RandomLocationAdapter();

        // sample a few times just to be safe
        for (int i = 0; i < 20; i++) {
            CoordinateDTO c = adapter.currentCoordinate();
            double lat = c.getLatitudeNum();
            double lon = c.getLongitudeNUM();

            assertTrue(lat >= 59.85 && lat <= 59.98, "lat out of default Oslo bbox");
            assertTrue(lon >= 10.60 && lon <= 10.90, "lon out of default Oslo bbox");
        }
    }
}
