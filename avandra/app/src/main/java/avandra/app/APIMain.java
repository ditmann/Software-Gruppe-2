package avandra.app;



import avandra.core.port.EnturClient;
import avandra.core.domain.Coordinate;
import avandra.core.domain.RandomLocationAdapter;
import avandra.api.EnturHttpClient;

import avandra.core.port.LocationPort;


public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        LocationPort location = new RandomLocationAdapter();
        Coordinate me = location.currentCoordinate();
        EnturClient entur = new EnturHttpClient(clientName);

        entur.planTripCoordsToFile(
                me.getLatitudeNum(), me.getLongitudeNUM(),   // from you
                59.136205, 11.382195,   // to moss// )
                3, // amount of different trips
                true                // include request metadata in file
        );
    }
}
