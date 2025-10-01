package no.avandra.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class JSONDBHandler extends DBHandler{
    @Override
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

    @Override
    public Object retrieveData() {


    }

    //  funksjon for å lagre destinasjoner til en JSON fil
    public static void saveDestinations (Destinasjon destinasjon) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            // convert Java object to JSON file
            mapper.writeValue(new File("saved Destinations.json"), destinasjon);
            // convert Java object to JSON string
            String jsonString = mapper.writeValueAsString(destinasjon);

            System.out.println(jsonString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
