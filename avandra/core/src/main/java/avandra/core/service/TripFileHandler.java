package avandra.storage.adapter;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import avandra.core.domain.Coordinate;
import avandra.core.domain.TripPart;
import avandra.core.port.EnturClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles planning trips via Entur and writing the trip JSON
 * Returns a matrix of legs so we keep each tripPattern grouped
 */
public final class TripFileHandler {
    private final EnturClient entur;
    private final ObjectMapper json;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Oslo");

    public TripFileHandler(EnturClient entur, ObjectMapper json) {
        this.entur = entur;
        this.json = json;
    }

    /**
     * Plan a trip from coords[0] (from) to coords[1] (to), write Trip.json,
     * and return a matrix of trip parts where each inner list is one tripPattern
     * if Entur returns a single pattern you'll get a matrix with one inner list
     *
     * @param coords             list with two coordinates [from, to]
     * @param numPatterns        how many patterns Entur should return
     * @param includeRequestMeta if true we wrap the written file with a tiny request object for traceability
     * @return                   matrix of trip parts grouped by pattern
     * @throws IOException       if writing the file fails or the Entur call raises I/O errors
     * @throws IllegalArgumentException if coords is null or has fewer than two items
     */
    public List<List<TripPart>> planTrip(List<Coordinate> coords,
                                         int numPatterns,
                                         boolean includeRequestMeta) throws IOException {
        if (coords == null || coords.size() < 2) {
            throw new IllegalArgumentException("expected a list with exactly two coordinates [from, to]");
        }

        Coordinate from = coords.get(0);
        Coordinate to   = coords.get(1);

        // use the exact getters the Coordinate exposes
        double fromLat = from.getLatitudeNum();
        double fromLon = from.getLongitudeNUM();
        double toLat   = to.getLatitudeNum();
        double toLon   = to.getLongitudeNUM();

        // ask Entur for the raw trip node that contains tripPatterns
        JsonNode tripNode = entur.planTripCoords(fromLat, fromLon, toLat, toLon, numPatterns);

        // write Trip.json either as the raw trip node or wrapped with request for debugging
        File outFile = new File("Trip.json");
        JsonNode toWrite = includeRequestMeta
                ? withRequest(json, tripNode, fromLat, fromLon, toLat, toLon, numPatterns)
                : tripNode;
        json.writerWithDefaultPrettyPrinter().writeValue(outFile, toWrite);

        // return the grouped leg
        return parseTripPartsGrouped(tripNode);
    }

    /**
     * Build a wrapper JSON so we can see what we asked for alongside the response
     */
    private static JsonNode withRequest(ObjectMapper json, JsonNode tripNode,
                                        double fromLat, double fromLon,
                                        double toLat, double toLon, int n) {
        var root = json.createObjectNode();
        var req = root.putObject("request");
        req.put("fromLat", fromLat).put("fromLon", fromLon)
                .put("toLat", toLat).put("toLon", toLon)
                .put("numPatterns", n);
        root.set("trip", tripNode);
        return root;
    }

    /**
     * Parse the Entur trip node into a matrix where each inner list is one pattern's legs
     */
    private static List<List<TripPart>> parseTripPartsGrouped(JsonNode tripNode) {
        List<List<TripPart>> grouped = new ArrayList<>();

        JsonNode patterns = tripNode.path("tripPatterns");
        for (JsonNode pattern : patterns) {
            List<TripPart> legsForPattern = new ArrayList<>();
            JsonNode legs = pattern.path("legs");
            for (JsonNode leg : legs) {
                legsForPattern.add(parseSingleLeg(leg));
            }
            grouped.add(legsForPattern);
        }

        return grouped;
    }

    /**
     * Map a single leg node to TripPart
     */
    private static TripPart parseSingleLeg(JsonNode leg) {
        TripPart tripPart = new TripPart();

        // mode like bus or foot
        tripPart.setLegTransportMode(leg.path("mode").asText());

        // distance in meters
        tripPart.setTravelDistance(leg.path("distance").asInt());

        // line details are optional so guard them
        JsonNode line = leg.path("line");
        if (!line.isMissingNode() && !line.isNull()) {
            tripPart.setLineId(line.path("id").asText());
            tripPart.setLineName(line.path("name").asText());
            tripPart.setLineNumber(line.path("publicCode").asText());
            tripPart.setLineTransportMode(line.path("transportMode").asText());

            JsonNode authority = line.path("authority");
            tripPart.setLineOwner(authority.path("name").asText());
        }

        // fromEstimatedCall platform and times
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

        // toEstimatedCall platform and arrival times
        JsonNode toEstimatedCall = leg.path("toEstimatedCall");
        if (!toEstimatedCall.isMissingNode() && !toEstimatedCall.isNull()) {
            JsonNode toQuay = toEstimatedCall.path("quay");
            tripPart.setArrivePlatformId(toQuay.path("id").asText());
            tripPart.setArrivePlatformName(toQuay.path("name").asText());

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

        return tripPart;
    }
}
