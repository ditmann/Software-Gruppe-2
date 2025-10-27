package avandra.app;



import java.io.File;

import avandra.storage.adapter.MongoDBConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import avandra.api.EnturHttpClient;
import avandra.core.adapter.RandomLocationAdapter;
import avandra.core.domain.Coordinate;
import avandra.core.domain.TripParser;
import avandra.core.domain.TripPart;
import avandra.core.port.DBHandler;
import avandra.core.port.EnturClient;
import avandra.core.port.LocationPort;
import avandra.storage.adapter.MongoDBHandler;
import avandra.storage.adapter.TripFileHandler;

/*
public class APIMain {
    public static void main(String[] args) throws Exception {
        String clientName = "HIOFsTUD-AVANDRA";
        LocationPort location = new RandomLocationAdapter();
        Coordinate me = location.currentCoordinate();
        EnturClient entur = new EnturHttpClient(clientName);
        MongoDBConnection connection = new MongoDBConnection();
        DBHandler handler = new MongoDBHandler(connection);
        Coordinate to = handler.searchDestination("Kåre","hjem");
        TripFileHandler files = new TripFileHandler(entur, new ObjectMapper());

        File json = files.planTripCoordsToFile(
                me.getLatitudeNum(), me.getLongitudeNUM(),   // from you
                to.getLatitudeNum(), to.getLongitudeNUM(), // your chosen destination
                1,        // amount of different trips
                true                // include request metadata in file
        );
        TripParser parser = new TripParser();

        for(TripPart part : parser.tripParts(json)) {
            System.out.println(part.toString());
        }
    }
}

 */