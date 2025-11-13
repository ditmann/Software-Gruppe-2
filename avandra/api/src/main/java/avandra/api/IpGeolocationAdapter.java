package avandra.api;

import java.io.IOException;
import java.time.Duration;

import avandra.core.DTO.CoordinateDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import avandra.core.port.LocationPort;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adapter that fetches an approximate location from ipapi
 * built for our MVP and easy to swap because it implements LocationPort
 * default endpoint is ipapi.co/json and we send a simple user agent
 */
public class IpGeolocationAdapter implements LocationPort {

    private final String clientName;

    // default endpoint for prod
    private static final String DEFAULT_ENDPOINT = "https://ipapi.co/json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OkHttpClient http;
    private final String endpoint;

    /**
     * normal constructor used by the app
     * @param clientName value for the User Agent header so the api knows who calls
     */
    public IpGeolocationAdapter(String clientName) {
        this.clientName = clientName;
        this.http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(6))
                .build();
        this.endpoint = DEFAULT_ENDPOINT;
    }

    /**
     * extra constructor for tests where we inject a client and endpoint
     * pass null for http or endpoint to fall back to defaults
     * @param clientName value for the User Agent header
     * @param http custom OkHttpClient to use in tests
     * @param endpoint base url to call like http://localhost:xxxx/json when using a mock server
     */
    public IpGeolocationAdapter(String clientName, OkHttpClient http, String endpoint) {
        this.clientName = clientName;
        this.http = http != null ? http : new OkHttpClient();
        this.endpoint = endpoint != null ? endpoint : DEFAULT_ENDPOINT;
    }

    /**
     * calls ipapi to get the current coordinate for this client
     * returns latitude and longitude as floats wrapped in CoordinateDTO
     * treats non 2xx responses and empty bodies as errors
     * @return CoordinateDTO with latitude and longitude from the response
     * @throws IOException when the network call fails or the json parsing fails
     * @throws IllegalStateException when the server returns a non successful http status
     */
    @Override
    public CoordinateDTO currentCoordinate() throws Exception {
        Request req = new Request.Builder()
                .url(endpoint)
                .header("User-Agent", clientName)
                .build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new IllegalStateException("ipapi.co HTTP " + res.code());
            }
            if (res.body() == null) {
                throw new IOException("Empty response body from ipapi.co");
            }

            JsonNode json = MAPPER.readTree(res.body().byteStream());
            double lat = json.get("latitude").asDouble();
            double lon = json.get("longitude").asDouble();

            return new CoordinateDTO((float) lat, (float) lon);
        }
    }
}
