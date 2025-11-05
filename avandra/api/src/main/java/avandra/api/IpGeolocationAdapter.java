package avandra.api;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import avandra.core.domain.Coordinate;
import avandra.core.port.LocationPort;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IpGeolocationAdapter implements LocationPort {

    private final String clientName;

    // default endpoint for prod
    private static final String DEFAULT_ENDPOINT = "https://ipapi.co/json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OkHttpClient http;
    private final String endpoint;

    // normal constructor
    public IpGeolocationAdapter(String clientName) {
        this.clientName = clientName;
        this.http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(6))
                .build();
        this.endpoint = DEFAULT_ENDPOINT;
    }

    // extra constructor for tests so we can inject a mock server and client
    public IpGeolocationAdapter(String clientName, OkHttpClient http, String endpoint) {
        this.clientName = clientName;
        this.http = http != null ? http : new OkHttpClient();
        this.endpoint = endpoint != null ? endpoint : DEFAULT_ENDPOINT;
    }

    @Override
    public Coordinate currentCoordinate() throws Exception {
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

            return new Coordinate((float) lat, (float) lon);
        }
    }
}
