package avandra.storage.adapter;

import java.io.File;
import java.util.List;

import avandra.core.port.DBHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import avandra.core.domain.AdminBruker;
import avandra.core.domain.Coordinate;
import avandra.core.domain.Destinasjon;
import avandra.core.domainParents.Bruker;


public class JSONDBHandler implements DBHandler {

    ///creates new file /overwrites each time
    //fine for testing
    public void sendData(String key, Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(key), object);
            String jsonString = mapper.writeValueAsString(object);
            System.out.println(jsonString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createUser(String userID, boolean adminUser, String favoriteDestination, String address, double latitude, double longitude) {

    }

    /// Denne gjør ingenting.
    /// prøvde å legge til noe i objektet "bruker" men det er jo dumt. skal dokumentet VÆRE en versjon av bruker?
    /// ignore
    public void appendData(String filepath, String addKey, Object addValue) {
        ObjectMapper mapper = new ObjectMapper();
        Bruker bruker = new AdminBruker("Moiraine");
        try {
            File file = new File(filepath);
            bruker = mapper.readValue(file, AdminBruker.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Returns the JSON doc contents
    public Object retrieveAllData() {
        Destinasjon destinasjon = new Destinasjon();
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("dummydata.json");
            destinasjon = mapper.readValue(file, Destinasjon.class);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return destinasjon;
    }

    @Override
    public void addDestinationToFavorites(String userID, String destinationName, String address, double latitude, double longitude) {
    }

    public Coordinate destinationCoordinate(String name) {
        return null;
    }

    @Override
    public void addCoordinatesToDestination(String userID, String destinationName, double latitude, double longitude) {

    }

    @Override
    public Coordinate searchDestination(String userID, String destinationID) {
        return null;
    }


    @Override
    public void removeData(String userID) {

    }

    @Override
    public void removeData(String userID, String keyToRemove) {

    }

    @Override
    public void removeData(String userID, String keyToRemove, String destinationType) {

    }

    @Override
    public void removeData(String userID, String keyToRemove, String destinationType, String destinationKey) {

    }

    @Override
    public boolean insertDestinationForLiteUser(String liteUserId, String destId, String name, String address, Double lat, Double lng, String adminId) {
        return false;
    }
/*
    {"latitudeN":true,
    "latitudeNum":59.9139,
    "longitudeE":true,
    "longitudeNUM":10.7522,
    "validert_bool":true}*/


}