package no.avandra.classes;



public class Destinasjon {


    //Koordinater fra Google:
    private boolean  latitudeN;
    private float latitudeNum;
    private boolean  longitudeE;
    private float longitudeNUM;


    private Boolean validert_bool;


    public Destinasjon(boolean latitudeN, float latitudeNum, boolean longitudeE, float longitudeNUM, Boolean validert_bool) {
        this.latitudeN = latitudeN;
        this.latitudeNum = latitudeNum;
        this.longitudeE = longitudeE;
        this.longitudeNUM = longitudeNUM;
        this.validert_bool = validert_bool;
    }


    public boolean isLatitudeN() {
        return latitudeN;
    }

    public void setLatitudeN(boolean latitudeN) {
        this.latitudeN = latitudeN;
    }

    public float getLatitudeNum() {
        return latitudeNum;
    }

    public void setLatitudeNum(float latitudeNum) {
        this.latitudeNum = latitudeNum;
    }

    public boolean isLongitudeE() {
        return longitudeE;
    }

    public void setLongitudeE(boolean longitudeE) {
        this.longitudeE = longitudeE;
    }

    public float getLongitudeNUM() {
        return longitudeNUM;
    }

    public void setLongitudeNUM(float longitudeNUM) {
        this.longitudeNUM = longitudeNUM;
    }

    public Boolean getValidert_bool() {
        return validert_bool;
    }

    public void setValidert_bool(Boolean validert_bool) {
        this.validert_bool = validert_bool;
    }
}
