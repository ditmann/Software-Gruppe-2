package avandra.app;



import avandra.api.EnturHttpClient;
import avandra.core.domain.Coordinate;
import avandra.core.domain.IpGeolocationAdapter;
import avandra.core.port.DBHandler;
import avandra.core.port.EnturClient;
import avandra.core.port.LocationPort;
import avandra.storage.adapter.MongoDBHandler;


public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        LocationPort location = new IpGeolocationAdapter(clientName);
        Coordinate me = location.currentCoordinate();
        EnturClient entur = new EnturHttpClient(clientName);
        DBHandler handler = new MongoDBHandler();
        Coordinate to = (Coordinate) handler.searchDestination("Kåre", "favoritter","hjem");

        entur.planTripCoordsToFile(
                me.getLatitudeNum(), me.getLongitudeNUM(),   // from you
                to.getLatitudeNum(), to.getLongitudeNUM(),   // to mysen// )
                1, // amount of different trips
                true                // include request metadata in file
        );
    }
}
