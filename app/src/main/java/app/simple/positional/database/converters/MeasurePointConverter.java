package app.simple.positional.database.converters;

import androidx.room.TypeConverter;

import app.simple.positional.model.MeasurePoint;

public class MeasurePointConverter {

    @TypeConverter
    public String fromMeasurePoint(MeasurePoint measurePoint) {
        return measurePoint.convertForDatabase();
    }

    @TypeConverter
    public MeasurePoint toMeasurePoint(String data) {
        return MeasurePoint.convertFromDatabase(data);
    }
}
