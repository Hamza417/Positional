package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import app.simple.positional.database.converters.MeasurePointConverter;

@Entity(tableName = "measures")
public class Measure {

    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "date_added")
    long dateCreated;

    @ColumnInfo(name = "measure_name")
    String name;

    @ColumnInfo(name = "measure_note")
    String note;

    @TypeConverters(MeasurePointConverter.class)
    @ColumnInfo(name = "measure_points")
    MeasurePoint measurePoints;

    public Measure(int id, long dateCreated, String name, String note, MeasurePoint measurePoints) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.name = name;
        this.note = note;
        this.measurePoints = measurePoints;
    }

    public Measure() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public MeasurePoint getMeasurePoints() {
        return measurePoints;
    }

    public void setMeasurePoints(MeasurePoint measurePoints) {
        this.measurePoints = measurePoints;
    }
}
