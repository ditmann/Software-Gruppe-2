
package no.avandra.classes;

import java.util.concurrent.ThreadLocalRandom;

/** Returns fixed or random coords. */
public class RandomLocationAdapter implements LocationPort {

    private final Double fixedLat;  // nullable
    private final Double fixedLon;  // nullable
    private final double minLat, maxLat, minLon, maxLon;

    public RandomLocationAdapter(Double fixedLat, Double fixedLon) {
        this.fixedLat = fixedLat;
        this.fixedLon = fixedLon;
        // Oslo-ish bbox as a sane default
        this.minLat = 59.85; this.maxLat = 59.98;
        this.minLon = 10.60; this.maxLon = 10.90;
    }

    public RandomLocationAdapter() { this(null, null); }

    @Override
    public Coordinate currentCoordinate() {
        double lat = fixedLat != null ? fixedLat
                : ThreadLocalRandom.current().nextDouble(minLat, maxLat);
        double lon = fixedLon != null ? fixedLon
                : ThreadLocalRandom.current().nextDouble(minLon, maxLon);
        return new Coordinate((float) lat, (float)lon);
    }
}
