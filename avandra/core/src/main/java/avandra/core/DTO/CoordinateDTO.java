package avandra.core.DTO;

public class CoordinateDTO {
    private double latitudeNum;
    private double longitudeNUM;

    public CoordinateDTO(double latitudeNum, double longitudeNUM) {
        this.latitudeNum = latitudeNum;
        this.longitudeNUM = longitudeNUM;
    }
    public CoordinateDTO() {

    }

    @Override
    public String toString() {
        return latitudeNum + " " + longitudeNUM;
    }

    public double getLatitudeNum() {
        return latitudeNum;
    }

    public void setLatitudeNum(float latitudeNum) {
        this.latitudeNum = latitudeNum;
    }

    public double getLongitudeNUM() {
        return longitudeNUM;
    }

    public void setLongitudeNUM(float longitudeNUM) {
        this.longitudeNUM = longitudeNUM;
    }
}
