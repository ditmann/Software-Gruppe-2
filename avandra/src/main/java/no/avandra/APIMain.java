package no.avandra;



import no.avandra.classes.*;


public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        LocationPort location = new IpGeolocationAdapter(clientName);
        Coordinate me = location.currentCoordinate();
        EnturClient entur = new EnturHttpClient(clientName);

        entur.planTripCoordsToFile(
                me.getLatitudeNum(), me.getLongitudeNUM(),   // from you
                59.553999, 11.334520,   // to mysen// )
                1, // amount of different trips
                true                // include request metadata in file
        );
    }
}
