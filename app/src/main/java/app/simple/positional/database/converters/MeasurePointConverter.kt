package app.simple.positional.database.converters;

import androidx.room.TypeConverter;

import java.util.ArrayList;

import app.simple.positional.model.MeasurePoint;

public class MeasurePointConverter {

    @TypeConverter
    public String fromMeasurePoints(ArrayList<MeasurePoint> measurePoints) {
        StringBuilder stringBuilder = new StringBuilder();

        if (measurePoints != null && !measurePoints.isEmpty()) {
            for (MeasurePoint point : measurePoints) {
                stringBuilder.append(point.convertForDatabase()).append(";");
            }
        } else {
            return null;
        }

        return stringBuilder.toString();
    }

    @TypeConverter
    public ArrayList<MeasurePoint> toMeasurePoints(String data) {
        ArrayList<MeasurePoint> measurePoints = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            String[] points = data.split(";");
            for (String point : points) {
                measurePoints.add(MeasurePoint.convertFromDatabase(point));
            }
        }

        return measurePoints;
    }
}
