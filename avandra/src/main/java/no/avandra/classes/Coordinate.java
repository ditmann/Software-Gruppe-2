package no.avandra.classes;

public class Coordinate {
    private float latitudeNum;
    private float longitudeNUM;

    public Coordinate(float latitudeNum, float longitudeNUM) {
        this.latitudeNum = latitudeNum;
        this.longitudeNUM = longitudeNUM;
    }

    public float getLatitudeNum() {
        return latitudeNum;
    }

    public void setLatitudeNum(float latitudeNum) {
        this.latitudeNum = latitudeNum;
    }

    public float getLongitudeNUM() {
        return longitudeNUM;
    }

    public void setLongitudeNUM(float longitudeNUM) {
        this.longitudeNUM = longitudeNUM;
    }
}
