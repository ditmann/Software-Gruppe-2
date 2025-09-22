package no.avandra.classes;

import java.util.ArrayList;

public class AdminBruker extends  Bruker {

    private ArrayList<LiteBruker> listOfLite_aList;

    public AdminBruker(String navn_str, String passord_str, String id_str, String tlf_str, String ePost_str, float hastighetsfaktor_float, int filtreringPreferanse_int, ArrayList<LiteBruker> listOfLite_aList) {
        super(navn_str, passord_str, id_str, tlf_str, ePost_str, hastighetsfaktor_float, filtreringPreferanse_int);
        this.listOfLite_aList = listOfLite_aList;
    }

    public void addLiteBruker(LiteBruker liteBruker) {
        listOfLite_aList.add(liteBruker);
    }
}
