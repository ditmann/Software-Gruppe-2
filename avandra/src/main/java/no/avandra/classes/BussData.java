package no.avandra.classes;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class BussData {

    private String busRouteID;
    private PublicTransportStop busStop;



    private void getBusData(File busData){
        ObjectMapper objectMapper = new ObjectMapper();
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



   /*
   try {
            List<Reise> reiser = objectMapper.readValue(file, new TypeReference<List<Reise>>() {});
            for (Reise r : reiser) this.addFavReise(r);
        }catch (Exception e){
            System.err.println(e.getMessage());
*/


}
