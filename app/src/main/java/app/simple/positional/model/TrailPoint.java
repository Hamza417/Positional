package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trail_data")
public class TrailPoint {

    /**
     * Latitude of the trail
     */
    @ColumnInfo(name = "lat")
    double latitude;

    /**
     * Longitude of the trail
     */
    @ColumnInfo(name = "lng")
    double longitude;

    /**
     * Time of when this trail marker is added
     */
    @PrimaryKey
    @ColumnInfo(name = "time_added")
    long timeAdded;


    /**
     * Use {@link app.simple.positional.constants.TrailIcons} to fetch
     * icon drawable resource ID
     */
    @ColumnInfo(name = "icon_position")
    int iconPosition;

    /**
     * Notes related to current trail mark
     */
    @ColumnInfo(name = "note")
    String note;

    /**
     * Name of the current location marked
     */
    @ColumnInfo(name = "name")
    String name;

    /**
     * Accuracy of the marker's location
     */
    @ColumnInfo(name = "accuracy")
    float accuracy;

    public TrailPoint(double latitude, double longitude, long timeAdded, int iconPosition, String note, String name, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeAdded = timeAdded;
        this.iconPosition = iconPosition;
        this.note = note;
        this.name = name;
        this.accuracy = accuracy;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
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

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public int getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(int iconPosition) {
        this.iconPosition = iconPosition;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
