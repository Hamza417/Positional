package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.simple.positional.database.converters.MeasurePointConverter
import app.simple.positional.database.dao.MeasureDao
import app.simple.positional.model.Measure

@Database(entities = [Measure::class], exportSchema = true, version = 1)
@TypeConverters(MeasurePointConverter::class)
abstract class MeasureDatabase : RoomDatabase() {

    abstract fun measureDao(): MeasureDao?

    companion object {
        private var instance: MeasureDatabase? = null
        private const val DB_NAME = "measure_db"

        @Synchronized
        fun getInstance(context: Context, databaseName: String = this.DB_NAME): MeasureDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context, MeasureDatabase::class.java, databaseName)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}
