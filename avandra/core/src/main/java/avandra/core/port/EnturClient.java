package avandra.core.port;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * - Build and execute trip-planning requests (GraphQL under the hood).
 * - Provide a simple API for trip planning by coordinates.
 * Notes:
 * - Implementations should set Entur-required headers (e.g., "ET-Client-Name").
 * - Return the "trip" node (i.e., GraphQL data.trip) as a JsonNode.
 * - Do not perform file I/O here—keep this port purely about fetching data.
 */
public interface EnturClient {

    /**
     * Query Entur for trip plans between two coordinates.
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
