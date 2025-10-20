package avandra.core.port;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

/** Interface coordinate-based trip planning */
public interface EnturClient {
    /** Call Entur and return the "trip" node */
    JsonNode planTripCoords(double fromLat, double fromLon,
                            double toLat, double toLon,
                            int numPatterns) throws IOException;

    /**
     * Call Entur and write the JSON to a file (inside the client)
     * @param fromLat start latitude
     * @param fromLon start longitude
     * @param toLat   end latitude
     * @param toLon   end longitude
     * @param numPatterns number of itineraries to request
     * @param includeRequestMeta include the request (coords/numPatterns) alongside the trip in the file
     * @return the output file path
     */
    File planTripCoordsToFile(double fromLat, double fromLon,
                              double toLat, double toLon,
                              int numPatterns,
                              boolean includeRequestMeta) throws IOException;
}
