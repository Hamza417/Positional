package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "trail_data")
public class TrailData {

    /**
     * Latitude and longitude of the trail
     */
    @ColumnInfo(name = "lat_lng")
    LatLng latLng;

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

    public TrailData(LatLng latLng, long timeAdded, int iconPosition, String note, String name) {
        this.latLng = latLng;
        this.timeAdded = timeAdded;
        this.iconPosition = iconPosition;
        this.note = note;
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
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
