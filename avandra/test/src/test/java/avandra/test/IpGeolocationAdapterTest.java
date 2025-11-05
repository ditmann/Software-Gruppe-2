package avandra.test;

import avandra.core.adapter.IpGeolocationAdapter;
import avandra.core.DTO.CoordinateDTO;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IpGeolocationAdapterTest {

    private MockWebServer server;
    private OkHttpClient client;

    @BeforeAll
    void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new OkHttpClient();
    }

    @AfterAll
    void stopServer() throws Exception {
        server.shutdown();
    }

    @Test
    void parsesLatLonFromJson() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"latitude\":59.9139,\"longitude\":10.7522}"));

        var adapter = new IpGeolocationAdapter(
                "test-agent",
                client,
                server.url("/json").toString()
        );

        CoordinateDTO c = adapter.currentCoordinate();

        assertEquals(59.9139, c.getLatitudeNum(), 1e-4);
        assertEquals(10.7522, c.getLongitudeNUM(), 1e-4);
    }

    @Test
    void non200Throws() {
        server.enqueue(new MockResponse().setResponseCode(503));

        var adapter = new IpGeolocationAdapter(
                "test-agent",
                client,
                server.url("/json").toString()
        );

        assertThrows(IllegalStateException.class, adapter::currentCoordinate);
    }
}
