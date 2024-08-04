package app.simple.positional.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;

import app.simple.positional.database.converters.MeasurePointConverter;

@Entity(tableName = "measures")
public class Measure {

    @PrimaryKey
    @ColumnInfo(name = "date_added")
    long dateCreated;

    @ColumnInfo(name = "measure_name")
    String name;

    @ColumnInfo(name = "measure_note")
    String note;

    @TypeConverters(MeasurePointConverter.class)
    @ColumnInfo(name = "measure_points")
    @Nullable
    ArrayList<MeasurePoint> measurePoints;

    public Measure(long dateCreated, String name, String note) {
        this.dateCreated = dateCreated;
        this.name = name;
        this.note = note;
    }

    public Measure() {

    }

    public Measure(String name, String note) {
        this.dateCreated = System.currentTimeMillis();
        this.name = name;
        this.note = note;
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

    @Nullable
    public ArrayList<MeasurePoint> getMeasurePoints() {
        return measurePoints;
    }

    public void setMeasurePoints(@Nullable ArrayList<MeasurePoint> measurePoints) {
        this.measurePoints = measurePoints;
    }

    @Override
    public String toString() {
        return "Measure{" +
                "dateCreated=" + dateCreated +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                ", measurePoints=" + measurePoints +
                '}';
    }
}
