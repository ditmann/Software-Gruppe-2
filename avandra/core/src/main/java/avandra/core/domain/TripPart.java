package no.avandra.classes;


import java.time.LocalDateTime;

public class TripPart {

    //Her starter LEG- objektet
    private String TravelMode;
    private int TravelDistance;

    //Her starter line liste, kan være NULL!
    private String lineId;
    private String lineName;
    private String lineNumber;
    private String transportMode;
    private String lineOwner;

    //Her starter fromEstimate, kan være NULL!
    //Platform:
    private String departPlatformId;
    private String departPlatformName;

    //Avgangstid:
    private LocalDateTime aimedDeparture;
    private LocalDateTime expectedDeparture;

    //Her starter toEstimate, kan være NULL!
    //Platform:
    private String arrivalPlatformId;
    private String arrivalPlatformName;

    //Ankomsttid:
    private LocalDateTime aimedArrival;
    private LocalDateTime expectedArrival;


}
