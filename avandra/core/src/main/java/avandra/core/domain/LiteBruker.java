package avandra.core.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import avandra.core.domainParents.Bruker;
import avandra.core.domainParents.Reise;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LiteBruker extends Bruker{

    private ArrayList<AdminBruker> safeBruker_aList = new ArrayList<>();

    public LiteBruker(String navn_str, String passord_str, String id_str, String tlf_str, String ePost_str, float hastighetsfaktor_float, int filtreringPreferanse_int) {
        super(navn_str, passord_str, id_str, tlf_str, ePost_str, hastighetsfaktor_float, filtreringPreferanse_int);
    }

    public ArrayList<AdminBruker> getSafeBruker_aList() {
        return new ArrayList<>(safeBruker_aList);
    }

    public void addSafeBruker(AdminBruker adminBruker){safeBruker_aList.add(adminBruker);}


    private void getInfoFromAdmin(File file) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        try {
            List<Reise> reiser = objectMapper.readValue(file, new TypeReference<List<Reise>>() {});
            for (Reise r : reiser) this.addFavReise(r);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }


    }


}