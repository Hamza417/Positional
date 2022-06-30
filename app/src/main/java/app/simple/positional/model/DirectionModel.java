package app.simple.positional.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "directions")
public class DirectionModel implements Parcelable {

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "name")
    private String name;

    @PrimaryKey
    @ColumnInfo(name = "date_added")
    private long dateAdded;

    public DirectionModel(double latitude, double longitude, String name, long dateAdded) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.dateAdded = dateAdded;
    }

    public DirectionModel() {
    }

    protected DirectionModel(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        name = in.readString();
        dateAdded = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(name);
        dest.writeLong(dateAdded);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DirectionModel> CREATOR = new Creator<>() {
        @Override
        public DirectionModel createFromParcel(Parcel in) {
            return new DirectionModel(in);
        }

        @Override
        public DirectionModel[] newArray(int size) {
            return new DirectionModel[size];
        }
    };

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }
}
