package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measures",
        foreignKeys = @androidx.room.ForeignKey(entity = MeasurePoint.class,
                parentColumns = "time_added",
                childColumns = "measure_points_id",
                onDelete = androidx.room.ForeignKey.CASCADE))
public class MeasureEntry {

    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "date_added")
    long dateCreated;

    @ColumnInfo(name = "measure_name")
    String name;

    @ColumnInfo(name = "measure_note")
    String note;

    @ColumnInfo(name = "measure_points_id")
    long measurePointsId;

    public MeasureEntry(int id, long dateCreated, String name, String note, long measurePointsId) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.name = name;
        this.note = note;
        this.measurePointsId = measurePointsId;
    }

    public MeasureEntry() {
    }

    public int getId() {
        return id;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public long getMeasurePointsId() {
        return measurePointsId;
    }
}
