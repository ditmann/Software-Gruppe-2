package no.avandra.classes;



public class Destinasjon {
    
    private String posisjon_str;  //kordinater fra Google
    private Boolean validert_bool;

    public Destinasjon(String posisjon_str, Boolean validert_bool) {
        this.posisjon_str = posisjon_str;
        this.validert_bool = validert_bool;
    }

    public String getPosisjon_str() {
        return posisjon_str;
    }

    public void setPosisjon_str(String posisjon_str) {
        this.posisjon_str = posisjon_str;
    }

    public Boolean getValidert_bool() {
        return validert_bool;
    }

    public void setValidert_bool(Boolean validert_bool) {
        this.validert_bool = validert_bool;
    }
}
