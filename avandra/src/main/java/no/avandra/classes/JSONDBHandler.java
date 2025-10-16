package no.avandra.classes;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JSONDBHandler implements DBHandler{

    ///creates new file /overwrites each time
    //fine for testing
    public void createUser(String key, Object object) {
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


    public static void main(String[] args) {
        JSONDBHandler handler = new JSONDBHandler();
        System.out.println(handler.retrieveAllData());
    }
    /*
    {"latitudeN":true,
    "latitudeNum":59.9139,
    "longitudeE":true,
    "longitudeNUM":10.7522,
    "validert_bool":true}*/

}
