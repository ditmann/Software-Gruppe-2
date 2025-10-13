package no.avandra.classes;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.avandra.ports.LocationPort;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IpGeolocationAdapter implements LocationPort {

    private final String clientName;

    // Public IP geolocation endpoint returning JSON
    private static final String ENDPOINT = "https://ipapi.co/json";

    // Object mapper
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // HTTP client
    private final OkHttpClient http = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(6))
            .build();
    public IpGeolocationAdapter(String clientName) {
        this.clientName = clientName;
    }



    @Override
    public Coordinate currentCoordinate() throws Exception {
        // Building GET request
        Request req = new Request.Builder()
                .url(ENDPOINT)
                .header("User-Agent", clientName)
                .build();

        // Execute and parse!! try-with-resources to make it smoother
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new IllegalStateException("ipapi.co HTTP " + res.code());
            }

            if (res.body() == null) {
                // CHECK!!!! avoid NPE if provider returns no content
                throw new IOException("Empty response body from ipapi.co");
            }

            // Parse JSON once and read the fields
            JsonNode json = MAPPER.readTree(res.body().byteStream());

                double lat = json.get("latitude").asDouble();
                double lon = json.get("longitude").asDouble();

                // Converter
            return new Coordinate((float) lat, (float)lon);
        }
    }
}
