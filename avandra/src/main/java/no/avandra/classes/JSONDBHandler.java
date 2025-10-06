package no.avandra.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class JSONDBHandler implements DBHandler{

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

    public Object retrieveData() {
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
        System.out.println(handler.retrieveData());
    }
    /*
    {"latitudeN":true,
    "latitudeNum":59.9139,
    "longitudeE":true,
    "longitudeNUM":10.7522,
    "validert_bool":true}*/

}
