package app.simple.positional.database.instances

import TrailDataDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.model.TrailData

@Database(entities = [TrailData::class], exportSchema = false, version = 1)
abstract class TrailDataDatabase : RoomDatabase() {

    abstract fun trailDataDao(): TrailDataDao?

    companion object {
        private var instance: TrailDataDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String?): TrailDataDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context!!, TrailDataDatabase::class.java, DB_NAME!!)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}