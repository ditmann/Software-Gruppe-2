package avandra.core.DTO;

import java.time.LocalDateTime;

public class TripPartDTO {

    // Her starter LEG- objektet
    private String legTransportMode;
    private int travelDistance;

    // Her starter line liste, kan være NULL!
    private String lineId;
    private String lineName;
    private String lineNumber;
    private String lineTransportMode;
    private String lineOwner;

    // Her starter fromEstimate, kan være NULL!
    // Platform:
    private String departPlatformId;
    private String departPlatformName;

    // Avgangstid:
    private LocalDateTime aimedDeparture;
    private LocalDateTime expectedDeparture;

    // Her starter toEstimate, kan være NULL!
    // Platform:
    private String arrivePlatformId;
    private String arrivePlatformName;

    // Ankomsttid:
    private LocalDateTime aimedArrival;
    private LocalDateTime expectedArrival;




    public TripPartDTO() {}

    public String getLegTransportMode() {
        return legTransportMode;
    }

    public void setLegTransportMode(String legTransportMode) {
        this.legTransportMode = legTransportMode;
    }

    public String getLineTransportMode() {
        return lineTransportMode;
    }

    public void setLineTransportMode(String lineTransportMode) {
        this.lineTransportMode = lineTransportMode;
    }

    // Alias for legTransportMode
    public void setTravelMode(String travelMode) {
        this.legTransportMode = travelMode;
    }

    public int getTravelDistance() {
        return travelDistance;
    }

    public void setTravelDistance(int travelDistance) {
        this.travelDistance = travelDistance;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLineOwner() {
        return lineOwner;
    }

    public void setLineOwner(String lineOwner) {
        this.lineOwner = lineOwner;
    }

    public String getDepartPlatformId() {
        return departPlatformId;
    }

    public void setDepartPlatformId(String departPlatformId) {
        this.departPlatformId = departPlatformId;
    }

    public String getDepartPlatformName() {
        return departPlatformName;
    }

    public void setDepartPlatformName(String departPlatformName) {
        this.departPlatformName = departPlatformName;
    }

    public LocalDateTime getAimedDeparture() {
        return aimedDeparture;
    }

    public void setAimedDeparture(LocalDateTime aimedDeparture) {
        this.aimedDeparture = aimedDeparture;
    }

    public LocalDateTime getExpectedDeparture() {
        return expectedDeparture;
    }

    public void setExpectedDeparture(LocalDateTime expectedDeparture) {
        this.expectedDeparture = expectedDeparture;
    }

    public String getArrivePlatformId() {
        return arrivePlatformId;
    }

    public void setArrivePlatformId(String arrivalPlatformId) {
        this.arrivePlatformId = arrivalPlatformId;
    }

    public String getArrivePlatformName() {
        return arrivePlatformName;
    }

    public void setArrivePlatformName(String arrivalPlatformName) {
        this.arrivePlatformName = arrivalPlatformName;
    }

    public LocalDateTime getAimedArrival() {
        return aimedArrival;
    }

    public void setAimedArrival(LocalDateTime aimedArrival) {
        this.aimedArrival = aimedArrival;
    }

    public LocalDateTime getExpectedArrival() {
        return expectedArrival;
    }

    public void setExpectedArrival(LocalDateTime expectedArrival) {
        this.expectedArrival = expectedArrival;
    }


    @Override
    public String toString() {
        if(!(aimedArrival == null) && !(expectedArrival == null) && !(aimedDeparture == null) && !(expectedDeparture == null)) {
            String msg = departPlatformName + " " +  aimedDeparture.getHour()+":"+aimedDeparture.getMinute() + "---->" + " " + arrivePlatformName +" " + aimedArrival.getHour() + ":"+aimedArrival.getMinute() ;
            return msg;
    }else {
            return "walking" + " " + travelDistance + "m";
        }
}
}
