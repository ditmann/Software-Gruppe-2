package avandra.core.service;

import java.util.ArrayList;
import java.util.List;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.LocationPort;


public class JourneyPlannerService {

    LocationPort locationPort;
    DBService dbService;

    public JourneyPlannerService(LocationPort locationPort, DBService dbService) {
        this.locationPort = locationPort;
        this.dbService = dbService;
    }

    //startingpoint depends on what adapter you are using 
    public List<CoordinateDTO> fetchStartingPointAndEndPoint (String userIDForDB, String destinationID) throws Exception{
        List<CoordinateDTO> startingPointAndEndPoint = new ArrayList<>();
        CoordinateDTO startPoint = locationPort.currentCoordinate();
        CoordinateDTO endPoint = dbService.searchDestination(userIDForDB, destinationID);
        startingPointAndEndPoint.add(startPoint);
        startingPointAndEndPoint.add(endPoint);

        return startingPointAndEndPoint;
    }

}
