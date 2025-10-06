package no.avandra;



import no.avandra.classes.EnturHttpClient;
import no.avandra.entur.EnturClient;



public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        EnturClient entur = new EnturHttpClient(clientName);

        entur.planTripCoordsToFile(
                59.9139, 10.7522,   // from (Oslo approx)
                60.1976, 11.1004,   // to (OSL area approx)
                3, // amount of different trips
                true                // include request metadata in file
        );
    }
}
