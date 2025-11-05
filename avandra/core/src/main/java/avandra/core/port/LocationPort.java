package avandra.core.port;
import avandra.core.DTO.CoordinateDTO;

public interface LocationPort {
    /** Return the current device/user coordinate. */
    CoordinateDTO currentCoordinate() throws Exception;
}
