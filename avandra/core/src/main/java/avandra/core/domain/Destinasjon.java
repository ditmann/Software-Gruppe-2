package avandra.core.domain;

public class Destinasjon {

private Coordinate coordinate;
private Boolean validert_bool;

    public Destinasjon(Boolean validert_bool, Coordinate coordinate) {
        this.validert_bool = validert_bool;
        this.coordinate = coordinate;
    }

    public Destinasjon(){}


    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Boolean getValidert_bool() {
        return validert_bool;
    }

    public void setValidert_bool(Boolean validert_bool) {
        this.validert_bool = validert_bool;
    }
}
