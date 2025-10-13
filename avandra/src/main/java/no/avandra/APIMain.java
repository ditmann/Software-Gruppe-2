package no.avandra;



import no.avandra.classes.*;


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
