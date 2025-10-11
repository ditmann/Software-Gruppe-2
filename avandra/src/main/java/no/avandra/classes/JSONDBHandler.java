package no.avandra.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class JSONDBHandler implements DBHandler{

    ///creates new file /overwrites each time
    //fine for testing
    public void sendData(Object object, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(fileName), object);
            String jsonString = mapper.writeValueAsString(object);
            System.out.println(jsonString);
        }
        catch (Exception e) {
            e.printStackTrace();
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
