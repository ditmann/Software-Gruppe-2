package avandra.core.domain;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripParser {
private static final ObjectMapper MAPPER = new ObjectMapper();

public TripParser(){}

public List<TripPart> tripParts(File fileJSON){

    try(InputStream JSONIn = Files.newInputStream(fileJSON.toPath())) {
        JsonNode rootTree = MAPPER.readTree(JSONIn);
        JsonNode patterns = rootTree.path("trip").path("tripPatterns");

        //Oppretter liste over tripParts for trip.
        List<TripPart> parts = new ArrayList<>();

        //Finner "legs" i JSON- fil
        for (JsonNode pattern : patterns) {

            // Går inn i noden "legs"
            JsonNode legs = pattern.path("legs");

            //Itererer over listen legs fra JSON- fil
            for (JsonNode leg : legs) {

                //Oppretter  nytt tripPart- objekt
                TripPart tripPart = new TripPart();

                //Setter Mode i objektet til tekst fra JSON ("mode"):
                JsonNode legTransportMode = leg.path("mode");
                tripPart.setLegTransportMode(legTransportMode.asText());

                //Setter distance i objektet til en int fra JSON ("distance"):
                JsonNode  travelDistance = leg.path("distance");
                tripPart.setTravelDistance(travelDistance.asInt());

                //Går inn i noden "line"
                JsonNode line = leg.path("line");

                //Sjekker om noden er tom, og hopper over om den er det.
                if (!line.isMissingNode()) {

                    //Setter lineId i objektet til tekst fra JSON ("line"):
                    JsonNode lineId = line.path("id");
                    tripPart.setLineId(lineId.asText());

                    //setter lineName i objektet til tekst fra JSON ("name")
                    JsonNode lineName = line.path("name");
                    tripPart.setLineName(lineName.asText());

                    //setter lineNumber i objektet til tekst fra JSON ("publicCode")
                    JsonNode lineNumber = line.path("publicCode");
                    tripPart.setLineNumber(lineNumber.asText());

                    //setter transportMode i objektet til tekst fra JSON("transportMode")
                    JsonNode transportMode = line.path("transportMode");
                    tripPart.setLineTransportMode(transportMode.asText());

                    //setter lineOwner i objektet til tekst fra JSON("Authority" > "name")
                    JsonNode authority = line.path("authority");
                    JsonNode lineOwner = authority.path("name");
                    tripPart.setLineOwner(lineOwner.asText());
                }


                //Jobber videre i legs til "fromEstimatedCall"
                //Går inn i fromEstimatedCall
                JsonNode fromEstimatedCall = legs.path("fromEstimatedCall");




                //Går inn i "quay" i fromEstimatedCall
                JsonNode fromQuay = fromEstimatedCall.path("quay");
                JsonNode fromQuayId = fromQuay.path("id");
                tripPart.setDepartPlatformId(fromQuayId.asText());

                JsonNode fromQuayName = fromEstimatedCall.path("name");
                tripPart.setDepartPlatformName(fromQuayName.asText());

                // Format må konverteres til dateTime
                JsonNode aimedDepartureTime = fromEstimatedCall.path("aimedDepartureTime");
                tripPart.setAimedDeparture(OffsetDateTime.parse(aimedDepartureTime.asText()).toLocalDateTime());

                JsonNode expectedDepartureTime = fromEstimatedCall.path("expectedDepartureTime");
                tripPart.setExpectedDeparture(OffsetDateTime.parse(expectedDepartureTime.asText()).toLocalDateTime());


                //Jobber videre i legs til "toEstimatedCall"
                //Går inn i toEstimatedCall
                JsonNode toEstimatedCall= legs.path("toEstimatedCall");



                    //Går inn i "quay"
                    JsonNode toQuay = toEstimatedCall.path("quay");
                    JsonNode toQuayId = toQuay.path("id");
                    tripPart.setArrivePlatformId(toQuayId.asText());

                    JsonNode toQuayName = toQuay.path("name");
                    tripPart.setArrivePlatformName(toQuayName.asText());
            }

        }

    }catch (IOException e) {
        System.err.println(e.getMessage());
    }




    return null;
}
}
