package avandra.storage.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import avandra.core.port.DBHandlerPort;
import com.fasterxml.jackson.databind.ObjectMapper;


import avandra.core.DTO.CoordinateDTO;
import org.bson.Document;


public class JSONDBHandlerAdapter implements DBHandlerPort {

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

    @Override
    public void createUser(String userID, boolean adminUser) {

    }

    /// Denne gjør ingenting.
    /// prøvde å legge til noe i objektet "bruker" men det er jo dumt. skal dokumentet VÆRE en versjon av bruker?
    /// ignore
    public void appendData(String filepath, String addKey, Object addValue) {
      /*  ObjectMapper mapper = new ObjectMapper();
        Bruker bruker = new AdminBruker("Moiraine");
        try {
            File file = new File(filepath);
            bruker = mapper.readValue(file, AdminBruker.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } */
    }

    /// Returns the JSON doc contents
    public Object retrieveAllData() {
       /* Destinasjon destinasjon = new Destinasjon();
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("dummydata.json");
            destinasjon = mapper.readValue(file, Destinasjon.class);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return destinasjon; */
        return null;
    }

    @Override
    public void addDestinationToFavorites(String userID, String destinationName, String address, double latitude, double longitude) {
    }

    public CoordinateDTO destinationCoordinate(String name) {
        return null;
    }

    @Override
    public void addCoordinatesToFavDestination(String userID, String destinationName, double latitude, double longitude) {

    }

    @Override
    public CoordinateDTO searchFavDestination(String userID, String destinationID) {
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
    public List<String> listUserFavDestinations(String userId) {
        return null;
    }

    @Override
    public List<String> listLitebrukereForAdmin(String adminId) {
        return List.of();
    }



    @Override
    public boolean isAdmin(String userId) {
        return false;
    }
/*
    {"latitudeN":true,
    "latitudeNum":59.9139,
    "longitudeE":true,
    "longitudeNUM":10.7522,
    "validert_bool":true}*/


}