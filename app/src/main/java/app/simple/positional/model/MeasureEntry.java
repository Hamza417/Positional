package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measures")
public class MeasureEntry {

    @PrimaryKey(autoGenerate = true)
    public int keyTag = 0;

    @ColumnInfo(name = "date_added")
    long dateCreated;

    @ColumnInfo(name = "measure_name")
    String name;

    @ColumnInfo(name = "measure_note")
    String note;

    public MeasureEntry(long dateCreated, String name, String note) {
        this.dateCreated = dateCreated;
        this.name = name;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
