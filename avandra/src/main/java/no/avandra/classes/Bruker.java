package no.avandra.classes;

import java.util.ArrayList;

public abstract class Bruker {
    private String navn_str;
    private String passord_str;
    private String id_str;
    private String tlf_str;
    private String ePost_str;
    private ArrayList<Reise> favorittReise_aList = new ArrayList<>();
    private ArrayList<Reise> planlagtReise_aList = new ArrayList<>();
    private float hastighetsfaktor_float;
    private int filtreringPreferanse_int;

    public Bruker(String navn_str, String passord_str, String id_str, String tlf_str, String ePost_str, float hastighetsfaktor_float, int filtreringPreferanse_int) {
        this.navn_str = navn_str;
        this.passord_str = passord_str;
        this.id_str = id_str;
        this.tlf_str = tlf_str;
        this.ePost_str = ePost_str;
        this.hastighetsfaktor_float = hastighetsfaktor_float;
        this.filtreringPreferanse_int = filtreringPreferanse_int;
    }

    public void addFavReise(Reise reise) {
        favorittReise_aList.add(reise);
    }

    public void addPlanlagtReise(Reise reise) { planlagtReise_aList.add(reise); }

    public ArrayList<Reise> getFavorittReise_aList() {
        return new ArrayList<>(favorittReise_aList);
    }

    public ArrayList<Reise> getPlanlagtReise_aList() {
        return new ArrayList<>(planlagtReise_aList);
    }

    public float getHastighetsfaktor_float() {
        return hastighetsfaktor_float;
    }

    public void setHastighetsfaktor_float(float hastighetsfaktor_float) {
        this.hastighetsfaktor_float = hastighetsfaktor_float;
    }

    public int getFiltreringPreferanse_int() {
        return filtreringPreferanse_int;
    }

    public void setFiltreringPreferanse_int(int filtreringPreferanse_int) {
        this.filtreringPreferanse_int = filtreringPreferanse_int;
    }

    public String getePost_str() {
        return ePost_str;
    }

    public void setePost_str(String ePost_str) {
        this.ePost_str = ePost_str;
    }

    public String getTlf_str() {
        return tlf_str;
    }

    public void setTlf_str(String tlf_str) {
        this.tlf_str = tlf_str;
    }

    public String getId_str() {
        return id_str;
    }

    public void setId_str(String id_str) {
        this.id_str = id_str;
    }

    public String getPassord_str() {
        return passord_str;
    }

    public void setPassord_str(String passord_str) {
        this.passord_str = passord_str;
    }

    public String getNavn_str() {
        return navn_str;
    }

    public void setNavn_str(String navn_str) {
        this.navn_str = navn_str;
    }
}
