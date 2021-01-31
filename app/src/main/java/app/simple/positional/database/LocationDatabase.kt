package app.simple.positional.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.model.Locations

@Database(entities = [Locations::class], exportSchema = false, version = 1)
abstract class LocationDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao?

    companion object {
        private var instance: LocationDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String?): LocationDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context!!, LocationDatabase::class.java, DB_NAME!!)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance
        }
    }
}
