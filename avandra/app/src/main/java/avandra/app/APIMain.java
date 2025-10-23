package avandra.app;



import avandra.api.EnturHttpClient;
import avandra.core.adapter.RandomLocationAdapter;
import avandra.core.domain.Coordinate;
import avandra.core.port.DBHandler;
import avandra.core.port.EnturClient;
import avandra.core.port.LocationPort;
import avandra.storage.adapter.MongoDBHandler;
import avandra.storage.adapter.TripFileHandler;
import com.fasterxml.jackson.databind.ObjectMapper;


public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        LocationPort location = new RandomLocationAdapter();
        Coordinate me = location.currentCoordinate();
        EnturClient entur = new EnturHttpClient(clientName);
        DBHandler handler1 = new MongoDBHandler();
        Coordinate to = handler1.searchDestination("Kåre", "favoritter","hjem");
        TripFileHandler files = new TripFileHandler(entur, new ObjectMapper());

        files.planTripCoordsToFile(
                me.getLatitudeNum(), me.getLongitudeNUM(),   // from you
                to.getLatitudeNum(), to.getLongitudeNUM(),
                1, // amount of different trips
                true                // include request metadata in file
        );
    }
}
