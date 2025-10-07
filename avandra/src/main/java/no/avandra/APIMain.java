package no.avandra;



import no.avandra.classes.EnturHttpClient;
import no.avandra.classes.EnturClient;



public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        EnturClient entur = new EnturHttpClient(clientName);

        entur.planTripCoordsToFile(
                59.128697, 11.352571,   // from (HIOF)
                63.456743, 11.349487,   // to (tinas mom// )
                3, // amount of different trips
                true                // include request metadata in file
        );
    }
}
