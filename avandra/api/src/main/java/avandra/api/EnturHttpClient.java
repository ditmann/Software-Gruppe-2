package avandra.api;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import avandra.core.port.EnturClientPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import avandra.core.port.EnturClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * - Build and execute GraphQL requests against Entur.
 * - Provide a simple API for trip planning by coordinates.
 * - Saves the result to a JSON file.
 * Notes:
 * - Requires an "ET-Client-Name" header as per Entur API policy.
 * - Uses OkHttp for HTTP and Jackson for JSON.
 */
public final class EnturHttpClient implements EnturClientPort {

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

    public JsonNode planTripCoords(double fromLat, double fromLon,
                                   double toLat, double toLon,
                                   int numPatterns) throws IOException {
        // GraphQL query: requests high-level trip info and per-leg details.
        String gql =
                "query($fromLat:Float!,$fromLon:Float!,$toLat:Float!,$toLon:Float!,$n:Int!){"
                        + "  trip("
                        + "    from:{coordinates:{latitude:$fromLat,longitude:$fromLon}}"
                        + "    to:{coordinates:{latitude:$toLat,longitude:$toLon}}"
                        + "    numTripPatterns:$n"
                        + "    modes:{"
                        + "      accessMode: foot,"
                        + "      egressMode: foot,"
                        + "      transportModes:["
                        + "        {transportMode: bus},"
                        + "        {transportMode: rail},"
                        + "        {transportMode: tram},"
                        + "        {transportMode: metro},"
                        + "        {transportMode: coach},"
                        + "        {transportMode: water}"
                        + "      ]"
                        + "    }"
                        + "  ){"
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

    /* ---- Internal GraphQL helper ---- */

    /**
     * Builds the JSON payload, sets required headers, executes the HTTP call,
     * validates HTTP status and GraphQL errors, and returns the decoded JsonNode
     * DONT TOUCH
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