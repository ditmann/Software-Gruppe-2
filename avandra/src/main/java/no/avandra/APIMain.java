package no.avandra;



import no.avandra.classes.EnturHttpClient;
import no.avandra.classes.EnturClient;



public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        EnturClient entur = new EnturHttpClient(clientName);

        entur.planTripCoordsToFile(
                59.129133, 11.352893,   // from (HIOF)
                59.286139, 11.117261,   // to (tinas mom// )
                1, // amount of different trips
                true                // include request metadata in file
        );
    }
}
