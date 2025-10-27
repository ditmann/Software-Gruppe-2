package avandra.storage.adapter;
import java.io.File;
import java.io.IOException;
import avandra.core.port.EnturClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Ny klasse – enkel å teste
public final class TripFileHandler {
    private final EnturClient entur;
    private final ObjectMapper json;

    public TripFileHandler(EnturClient entur, ObjectMapper json) {
        this.entur = entur;
        this.json = json;
    }

    public File planTripCoordsToFile(double fromLat, double fromLon,
                                     double toLat, double toLon,
                                     int numPatterns,
                                     boolean includeRequestMeta) throws IOException {
        JsonNode tripNode = entur.planTripCoords(fromLat, fromLon, toLat, toLon, numPatterns);
        File outFile = new File("Trip.json");
        JsonNode toWrite = includeRequestMeta
                ? withRequest(json, tripNode, fromLat, fromLon, toLat, toLon, numPatterns)
                : tripNode;
        json.writerWithDefaultPrettyPrinter().writeValue(outFile, toWrite);
        return outFile;
    }

    private static JsonNode withRequest(ObjectMapper json, JsonNode tripNode,
                                        double fromLat, double fromLon,
                                        double toLat, double toLon, int n) {
        var root = json.createObjectNode();
        var req = root.putObject("request");
        req.put("fromLat", fromLat).put("fromLon", fromLon)
                .put("toLat", toLat).put("toLon", toLon)
                .put("numPatterns", n);
        root.set("trip", tripNode);
        return root;
    }
}

