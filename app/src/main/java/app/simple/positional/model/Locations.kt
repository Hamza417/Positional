package app.simple.positional.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
class Locations {
    @PrimaryKey
    @ColumnInfo(name = "date_added")
    var date = 0L

    @ColumnInfo(name = "latitude")
    var latitude = 0.0

    @ColumnInfo(name = "longitude")
    var longitude = 0.0

    @ColumnInfo(name = "address")
    var address = ""

    @ColumnInfo(name = "time_zone")
    var timeZone = ""
}