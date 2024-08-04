package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.database.dao.MeasurePointsDao
import app.simple.positional.model.MeasurePoint


@Database(entities = [MeasurePoint::class], exportSchema = true, version = 1)
abstract class MeasurePointDatabase : RoomDatabase() {

    abstract fun measurePointsDao(): MeasurePointsDao?

    companion object {
        private var instance: MeasurePointDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String?): MeasurePointDatabase? {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context!!, MeasurePointDatabase::class.java, DB_NAME!!)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance
        }
    }
}
