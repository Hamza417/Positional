package app.simple.positional.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trails")
public class Trails {

    @PrimaryKey
    @ColumnInfo(name = "date_added")
    long dateCreated;

    @ColumnInfo(name = "trail_name")
    String trailName;

    public Trails(long dateCreated, String trailName) {
        this.dateCreated = dateCreated;
        this.trailName = trailName;
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
}
