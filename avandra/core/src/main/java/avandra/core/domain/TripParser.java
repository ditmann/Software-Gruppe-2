package avandra.core.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TripParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Oslo");

    public TripParser(){}

    public List<TripPart> tripParts(File fileJSON){

        List<TripPart> parts = new ArrayList<>();

        try (InputStream JSONIn = Files.newInputStream(fileJSON.toPath())) {
            JsonNode rootTree = MAPPER.readTree(JSONIn);
            JsonNode patterns = rootTree.path("trip").path("tripPatterns");

            // Finner "legs" i JSON- fil
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
                    if (!line.isMissingNode() && !line.isNull()) {

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
                    JsonNode fromEstimatedCall = leg.path("fromEstimatedCall");
                    if (!fromEstimatedCall.isMissingNode() && !fromEstimatedCall.isNull()) {
                        JsonNode fromQuay = fromEstimatedCall.path("quay");
                        tripPart.setDepartPlatformId(fromQuay.path("id").asText());
                        tripPart.setDepartPlatformName(fromQuay.path("name").asText());

                        JsonNode aimedDepartureTime = fromEstimatedCall.path("aimedDepartureTime");
                        if (aimedDepartureTime.isTextual()) {
                            String s = aimedDepartureTime.asText().trim();
                            if (!s.isEmpty()) {
                                try {
                                    tripPart.setAimedDeparture(
                                            OffsetDateTime.parse(s)
                                                    .atZoneSameInstant(DEFAULT_ZONE)
                                                    .toLocalDateTime()
                                    );
                                } catch (Exception ignore) {}
                            }
                        }
                        JsonNode expectedDepartureTime = fromEstimatedCall.path("expectedDepartureTime");
                        if (expectedDepartureTime.isTextual()) {
                            String s = expectedDepartureTime.asText().trim();
                            if (!s.isEmpty()) {
                                try {
                                    tripPart.setExpectedDeparture(
                                            OffsetDateTime.parse(s)
                                                    .atZoneSameInstant(DEFAULT_ZONE)
                                                    .toLocalDateTime()
                                    );
                                } catch (Exception ignore) {}
                            }
                        }
                    }

                    //Jobber videre i legs til "toEstimatedCall"
                    //Går inn i toEstimatedCall
                    JsonNode toEstimatedCall = leg.path("toEstimatedCall");
                    if (!toEstimatedCall.isMissingNode() && !toEstimatedCall.isNull()) {
                        JsonNode toQuay = toEstimatedCall.path("quay");
                        tripPart.setArrivePlatformId(toQuay.path("id").asText());
                        tripPart.setArrivePlatformName(toQuay.path("name").asText());

                        // aimed ARRIVAL from aimedDepartureTime on toEstimatedCall
                        JsonNode aimedArrivalFromTo = toEstimatedCall.path("aimedDepartureTime");
                        if (aimedArrivalFromTo.isTextual()) {
                            String s = aimedArrivalFromTo.asText().trim();
                            if (!s.isEmpty()) {
                                try {
                                    tripPart.setAimedArrival(
                                            OffsetDateTime.parse(s)
                                                    .atZoneSameInstant(DEFAULT_ZONE)
                                                    .toLocalDateTime()
                                    );
                                } catch (Exception ignore) {}
                            }
                        }

                        // expected ARRIVAL from expectedDepartureTime on toEstimatedCall
                        JsonNode expectedArrivalFromTo = toEstimatedCall.path("expectedDepartureTime");
                        if (expectedArrivalFromTo.isTextual()) {
                            String s = expectedArrivalFromTo.asText().trim();
                            if (!s.isEmpty()) {
                                try {
                                    tripPart.setExpectedArrival(
                                            OffsetDateTime.parse(s)
                                                    .atZoneSameInstant(DEFAULT_ZONE)
                                                    .toLocalDateTime()
                                    );
                                } catch (Exception ignore) {}
                            }
                        }
                    }

                    parts.add(tripPart);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return parts;
    }
}
