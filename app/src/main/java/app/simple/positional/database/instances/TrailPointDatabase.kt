package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.database.dao.TrailPointDao
import app.simple.positional.model.TrailPoint

@Database(entities = [TrailPoint::class], exportSchema = true, version = 3)
abstract class TrailPointDatabase : RoomDatabase() {

    abstract fun trailDataDao(): TrailPointDao?

    companion object {
        private var instance: TrailPointDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String?): TrailPointDatabase? {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context!!, TrailPointDatabase::class.java, DB_NAME!!)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}
