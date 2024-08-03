package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measure_data")
public class MeasurePoint {

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
}
