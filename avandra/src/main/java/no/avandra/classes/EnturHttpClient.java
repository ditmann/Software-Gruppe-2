package no.avandra.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import no.avandra.classes.EnturClient;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * - Build and execute GraphQL requests against Entur.
 * - Provide a simple API for trip planning by coordinates.
 * - Saves the result to a JSON file.
 * Notes:
 * - Requires an "ET-Client-Name" header as per Entur API policy.
 * - Uses OkHttp for HTTP and Jackson for JSON.
 */
public final class EnturHttpClient implements EnturClient {

    // Entur Journey Planner GraphQL endpoint.
    private static final String GQL_URL = "https://api.entur.io/journey-planner/v3/graphql";

    // Reusable media type for JSON requests.
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    // HTTP client with timeouts tuned for network calls.
    private final OkHttpClient http;

    // JSON serializer/deserializer.
    private final ObjectMapper json;

    // Required client identification header value.
    private final String clientName;

    /**
     * Create a new Entur client.
     *
     * @param clientName Value for the ET-Client-Name header (must be non-blank).
     */
    public EnturHttpClient(String clientName) {
        if (clientName == null || clientName.isBlank()) {
            throw new IllegalArgumentException("ET-Client-Name required");
        }
        this.clientName = clientName;

        // Configure HTTP client timeouts:
        // - callTimeout: total allowed time per call
        // - connectTimeout: time to establish TCP connection
        this.http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(20))
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.json = new ObjectMapper();
    }

    /**
     * Query Entur for trip plans between two coordinates.
     *
     * @param fromLat     origin latitude
     * @param fromLon     origin longitude
     * @param toLat       destination latitude
     * @param toLon       destination longitude
     * @param numPatterns number of alternative trip patterns to request
     * @return the "trip" JsonNode from the GraphQL response (i.e., data.trip)
     * @throws IOException if the HTTP request fails or GraphQL returns errors
     */
    @Override
    public JsonNode planTripCoords(double fromLat, double fromLon,
                                   double toLat, double toLon,
                                   int numPatterns) throws IOException {
        // GraphQL query: requests high-level trip info and per-leg details.
        String gql =
                "query($fromLat:Float!,$fromLon:Float!,$toLat:Float!,$toLon:Float!,$n:Int!){"
                        + "  trip(from:{coordinates:{latitude:$fromLat,longitude:$fromLon}}"
                        + "       to:{coordinates:{latitude:$toLat,longitude:$toLon}}"
                        + "       numTripPatterns:$n){"
                        + "    tripPatterns{"
                        + "      startTime duration walkDistance"
                        + "      legs{"
                        + "        mode distance"
                        + "        line{id name publicCode transportMode authority{name}}"
                        + "        fromEstimatedCall{quay{id name} aimedDepartureTime expectedDepartureTime}"
                        + "        toEstimatedCall{quay{id name} aimedDepartureTime expectedDepartureTime}"
                        + "      }"
                        + "    }"
                        + "  }"
                        + "}";

        // Variables bound to the GraphQL query above.
        Map<String, Object> vars = Map.of(
                "fromLat", fromLat, "fromLon", fromLon,
                "toLat",   toLat,   "toLon",   toLon,
                "n",       numPatterns
        );

        // Execute and return the "trip" section only (data.trip).
        return graphql(gql, vars).path("data").path("trip");
    }

    /**
     * Same as {@link #planTripCoords}, but writes the response to a file.
     * You can optionally include the request metadata alongside the trip result
     *
     * @param fromLat           origin latitude
     * @param fromLon           origin longitude
     * @param toLat             destination latitude
     * @param toLon             destination longitude
     * @param numPatterns       number of alternatives
     * @param includeRequestMeta if true, writes {"request": {...}, "trip": <tripNode>}; otherwise writes just the trip node
     * @return the written file (currently "Trip.json" in the working directory)
     * @throws IOException if either the API call or file write fails
     */
    @Override
    public File planTripCoordsToFile(double fromLat, double fromLon,
                                     double toLat, double toLon,
                                     int numPatterns,
                                     boolean includeRequestMeta) throws IOException {
        // Call Entur API
        JsonNode tripNode = planTripCoords(fromLat, fromLon, toLat, toLon, numPatterns);

        // Output file target
        File outFile = new File("Trip.json");

        // Decide payload structure for disk:
        // - includeRequestMeta: write both the request parameters and the trip node
        // - otherwise: write only the trip node
        JsonNode toWrite;
        if (includeRequestMeta) {
            ObjectNode root = json.createObjectNode();
            ObjectNode req = root.putObject("request");
            req.put("fromLat", fromLat).put("fromLon", fromLon)
                    .put("toLat", toLat).put("toLon", toLon)
                    .put("numPatterns", numPatterns);
            // Important: attach the actual JsonNode (not a serialized string or file handle)
            root.set("trip", tripNode);
            toWrite = root;
        } else {
            toWrite = tripNode;
        }

        // Pretty-print JSON
        json.writerWithDefaultPrettyPrinter().writeValue(outFile, toWrite);

        return outFile;
    }

    /* ---- Internal GraphQL helper ---- */

    /**
     * Builds the JSON payload, sets required headers, executes the HTTP call,
     * validates HTTP status and GraphQL errors, and returns the decoded JsonNode
     *DONT TOUCH
     * @param query     GraphQL query string
     * @param variables Map of variables to bind to the query
     * @return root JsonNode of the response (already parsed)
     * @throws IOException on network failure, non-2xx status, or GraphQL "errors" present
     */
    private JsonNode graphql(String query, Map<String, Object> variables) throws IOException {
        // Build {"query": "...", "variables": {...}} payload
        JsonNode payload = json.createObjectNode()
                .put("query", query)
                .set("variables", json.valueToTree(variables));

        // Construct HTTP request with Entur-required headers
        Request req = new Request.Builder()
                .url(GQL_URL)
                .header("ET-Client-Name", clientName)     // Entur requires a client identifier
                .header("Content-Type", "application/json")
                .post(RequestBody.create(payload.toString(), JSON_MEDIA))
                .build();

        // Execute the call and ensure proper resource closing
        try (Response res = http.newCall(req).execute()) {
            // Fail fast on non-2xx responses. Include body if present for easier debugging
            if (!res.isSuccessful()) {
                String errBody = res.body() != null ? res.body().string() : "";
                throw new IOException("HTTP " + res.code() + (errBody.isEmpty() ? "" : (": " + errBody)));
            }

            // Parse response body as JSON
            String body = res.body() == null ? "" : res.body().string();
            JsonNode root = json.readTree(body);

            // GraphQL can return 200 with an "errors" array—treat as failure
            if (root.has("errors")) {
                throw new IOException("GraphQL errors: " + root.get("errors").toString());
            }

            return root;
        }
    }
}
