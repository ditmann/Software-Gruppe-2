package avandra.core.port;
import avandra.core.domain.Coordinate;

public interface LocationPort {
    /** Return the current device/user coordinate. */
    Coordinate currentCoordinate() throws Exception;
}
