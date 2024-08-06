package app.simple.positional.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MeasurePoint {

    private double latitude;
    private double longitude;
    private int order;

    public MeasurePoint(double latitude, double longitude, int order) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.order = order;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    @NonNull
    @Override
    public String toString() {
        return "MeasurePoint{" + "latitude=" + latitude + ", longitude=" + longitude + ", order=" + order + '}';
    }

    public String convertForDatabase() {
        return latitude + "," + longitude + "," + order;
    }

    public ArrayList<MeasurePoint> convertForDatabase(ArrayList<MeasurePoint> measurePoints) {
        return new ArrayList<>(measurePoints);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(getLatitude());
        result = 31 * result + Double.hashCode(getLongitude());
        result = 31 * result + getOrder();
        return result;
    }

    public static MeasurePoint convertFromDatabase(String data) {
        String[] parts = data.split(",");
        return new MeasurePoint(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Integer.parseInt(parts[2]));
    }
}
