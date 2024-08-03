package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trails")
public class TrailEntry {

    @PrimaryKey(autoGenerate = true)
    public int keyTag = 0;

    @ColumnInfo(name = "date_added")
    long dateCreated;

    @ColumnInfo(name = "trail_name")
    String trailName;

    @ColumnInfo(name = "trail_note")
    String trailNote;

    public TrailEntry(long dateCreated, String trailName, String trailNote) {
        this.dateCreated = dateCreated;
        this.trailName = trailName;
        this.trailNote = trailNote;
    }

    public String getTrailName() {
        return trailName;
    }

    public void setTrailName(String trailName) {
        this.trailName = trailName;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTrailNote() {
        return trailNote;
    }

    public void setTrailNote(String trailNote) {
        this.trailNote = trailNote;
    }
}
