package avandra.core.port;

import avandra.core.domain.Coordinate;

public interface SearchOnDestinationPort {

    Coordinate destinationCoordinate() throws Exception;

}
