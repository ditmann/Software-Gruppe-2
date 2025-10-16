package no.avandra.classes;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SearchOnDestination implements SearchOnDestinationPort {

    public SearchOnDestination(File getJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Coordinate> kordinater = objectMapper.readValue(getJSON, new TypeReference<List<Coordinate>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Coordinate destinationCoordinate(){
        return null;
    }
}
