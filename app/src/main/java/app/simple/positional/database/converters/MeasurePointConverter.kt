package app.simple.positional.database.converters

import androidx.room.TypeConverter
import app.simple.positional.model.MeasurePoint

class MeasurePointConverter {
    @TypeConverter
    fun fromMeasurePoints(measurePoints: ArrayList<MeasurePoint>?): String {
        val stringBuilder = StringBuilder()

        if (!measurePoints.isNullOrEmpty()) {
            for (point in measurePoints) {
                stringBuilder.append(point.convertForDatabase()).append(";")
            }
        } else {
            return ""
        }

        return stringBuilder.toString()
    }

    @TypeConverter
    fun toMeasurePoints(data: String?): ArrayList<MeasurePoint> {
        val measurePoints = ArrayList<MeasurePoint>()

        if (!data.isNullOrEmpty()) {
            val points = data.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (point in points) {
                measurePoints.add(MeasurePoint.convertFromDatabase(point))
            }
        }

        return measurePoints
    }
}
