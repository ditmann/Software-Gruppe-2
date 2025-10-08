package no.avandra.classes;
import no.avandra.classes.Coordinate;

public interface LocationPort {
    /** Return the current device/user coordinate. */
    Coordinate currentCoordinate() throws Exception;
}
