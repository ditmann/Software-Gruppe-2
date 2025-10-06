package no.avandra.classes;

public class Coordinate {
    private boolean  latitudeN;
    private float latitudeNum;
    private boolean  longitudeE;
    private float longitudeNUM;

    public Coordinate(boolean latitudeN, float latitudeNum, boolean longitudeE, float longitudeNUM) {
        this.latitudeN = latitudeN;
        this.latitudeNum = latitudeNum;
        this.longitudeE = longitudeE;
        this.longitudeNUM = longitudeNUM;
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
}
