package avandra.core.port;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * - Build and execute trip-planning requests (GraphQL under the hood)
 * - Provide a simple API for trip planning by coordinates
 * Notes:
 * - Implementations should set Entur-required headers ("ET-Client-Name").
 * - Return the "trip" node (GraphQL data.trip) as a JsonNode.
 */
public interface EnturClientPort {

    /**
     * Query Entur for trip plans between two coordinates
     *
     * @param fromLat     origin latitude
     * @param fromLon     origin longitude
     * @param toLat       destination latitude
     * @param toLon       destination longitude
     * @param numPatterns number of alternative trip patterns to request
     * @return the "trip" JsonNode from the GraphQL response (i.e., data.trip)
     * @throws IOException if the transport fails or the backend reports errors
     */
    JsonNode planTripCoords(double fromLat, double fromLon,
                            double toLat, double toLon,
                            int numPatterns) throws IOException;
}
