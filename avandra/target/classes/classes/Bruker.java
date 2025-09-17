package classes;

import java.util.ArrayList;

public abstract class Bruker {
    private String navn_str;
    private String passord_str;
    private String id_str;
    private String tlf_str;
    private String ePost_str;
    private ArrayList<Reise> favorittReise_aList;
    private ArrayList<Reise> planlagtReise_aList;
    private float hastighetsfaktor_fl;
    private int filtreringPreferanse_int;    

}
