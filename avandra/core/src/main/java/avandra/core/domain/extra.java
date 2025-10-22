/* package avandra.core.domain;

import avandra.core.domain.TripPart;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TripParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TripParser() {}

    public static List<TripPart> parseTripParts(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream in = Files.newInputStream(jsonFile.toPath())) {
            JsonNode root = mapper.readTree(in);
            JsonNode patterns = root.path("trip").path("tripPatterns");

            List<TripPart> parts = new ArrayList<>();
            if (patterns.isArray()) {
                for (JsonNode pattern : patterns) {
                    JsonNode legs = pattern.path("legs");
                    if (legs.isArray()) {
                        for (JsonNode leg : legs) {
                            TripPart tp = new TripPart();

                            // travel basics
                            {
                                JsonNode v = leg.path("mode");
                                tp.setTravelMode((v.isMissingNode() || v.isNull()) ? null : v.asText());
                            }
                            tp.setTravelDistance((int) Math.round(leg.path("distance").asDouble(0)));

                            // line info
                            JsonNode line = leg.path("line");
                            if (!line.isMissingNode() && !line.isNull()) {
                                JsonNode v;

                                v = line.path("id");
                                tp.setLineId((v.isMissingNode() || v.isNull()) ? null : v.asText());

                                v = line.path("name");
                                tp.setLineName((v.isMissingNode() || v.isNull()) ? null : v.asText());

                                v = line.path("publicCode");
                                tp.setLineNumber((v.isMissingNode() || v.isNull()) ? null : v.asText());

                                v = line.path("transportMode");
                                tp.setTransportMode((v.isMissingNode() || v.isNull()) ? null : v.asText());

                                JsonNode auth = line.path("authority");
                                if (!auth.isMissingNode() && !auth.isNull()) {
                                    v = auth.path("name");
                                    tp.setLineOwner((v.isMissingNode() || v.isNull()) ? null : v.asText());
                                }
                            }

                            // fromEstimatedCall (departure)
                            JsonNode from = leg.path("fromEstimatedCall");
                            if (!from.isMissingNode() && !from.isNull()) {
                                JsonNode quay = from.path("quay");
                                if (!quay.isMissingNode() && !quay.isNull()) {
                                    JsonNode v = quay.path("id");
                                    tp.setDepartPlatformId((v.isMissingNode() || v.isNull()) ? null : v.asText());

                                    v = quay.path("name");
                                    tp.setDepartPlatformName((v.isMissingNode() || v.isNull()) ? null : v.asText());
                                }

                                // aimedDepartureTime
                                {
                                    JsonNode v = from.path("aimedDepartureTime");
                                    String s = (v.isMissingNode() || v.isNull()) ? null : v.asText();
                                    tp.setAimedDeparture(s == null ? null : OffsetDateTime.parse(s).toLocalDateTime());
                                }
                                // expectedDepartureTime
                                {
                                    JsonNode v = from.path("expectedDepartureTime");
                                    String s = (v.isMissingNode() || v.isNull()) ? null : v.asText();
                                    tp.setExpectedDeparture(s == null ? null : OffsetDateTime.parse(s).toLocalDateTime());
                                }
                            }

                            // toEstimatedCall (arrival)
                            JsonNode to = leg.path("toEstimatedCall");
                            if (!to.isMissingNode() && !to.isNull()) {
                                JsonNode quay = to.path("quay");
                                if (!quay.isMissingNode() && !quay.isNull()) {
                                    JsonNode v = quay.path("id");
                                    tp.setArrivalPlatformId((v.isMissingNode() || v.isNull()) ? null : v.asText());

                                    v = quay.path("name");
                                    tp.setArrivalPlatformName((v.isMissingNode() || v.isNull()) ? null : v.asText());
                                }

                                // aimedArrival from "aimedDepartureTime"
                                {
                                    JsonNode v = to.path("aimedDepartureTime");
                                    String s = (v.isMissingNode() || v.isNull()) ? null : v.asText();
                                    tp.setAimedArrival(s == null ? null : OffsetDateTime.parse(s).toLocalDateTime());
                                }
                                // expectedArrival from "expectedDepartureTime"
                                {
                                    JsonNode v = to.path("expectedDepartureTime");
                                    String s = (v.isMissingNode() || v.isNull()) ? null : v.asText();
                                    tp.setExpectedArrival(s == null ? null : OffsetDateTime.parse(s).toLocalDateTime());
                                }
                            }

                            parts.add(tp);
                        }
                    }
                }
            }
            return parts;
        }
    }
}

}
*/