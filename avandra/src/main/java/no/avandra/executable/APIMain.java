package no.avandra.executable;



import no.avandra.classes.Coordinate;
import no.avandra.classes.RandomLocationAdapter;
import no.avandra.classes.api.EnturHttpClient;
import no.avandra.ports.EnturClient;
import no.avandra.ports.LocationPort;


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
