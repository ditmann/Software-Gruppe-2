package avandra.app;



import avandra.api.EnturHttpClient;
import avandra.core.domain.Coordinate;
import avandra.core.domain.IpGeolocationAdapter;
import avandra.core.port.LocationPort;
import avandra.storage.adapter.TripFileHandler;
import com.fasterxml.jackson.databind.ObjectMapper;


public class APIMain {
    public static <TripFileService> void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        LocationPort location = new IpGeolocationAdapter(clientName);
        Coordinate me = location.currentCoordinate();
        EnturHttpClient entur = new EnturHttpClient(clientName);
        TripFileHandler files = new TripFileHandler(entur, new ObjectMapper());
        files.planTripCoordsToFile(
                me.getLatitudeNum(), me.getLongitudeNUM(),
                59.553999, 11.334520,
                1,
                true
        );
    }
}
