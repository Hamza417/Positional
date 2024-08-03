package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.database.dao.TrailDataDao
import app.simple.positional.model.TrailPoint

@Database(entities = [TrailPoint::class], exportSchema = true, version = 3)
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
