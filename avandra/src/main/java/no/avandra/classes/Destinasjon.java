package no.avandra.classes;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.List;

public class Destinasjon {

private Coordinate coordinate;
private Boolean validert_bool;

    public Destinasjon(Boolean validert_bool, Coordinate coordinate) {
        this.validert_bool = validert_bool;
        this.coordinate = coordinate;
    }

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
