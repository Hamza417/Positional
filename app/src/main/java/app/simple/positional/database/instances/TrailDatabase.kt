package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.database.dao.TrailDao
import app.simple.positional.model.TrailEntry

@Database(entities = [TrailEntry::class], exportSchema = false, version = 1)
abstract class TrailDatabase : RoomDatabase() {

    abstract fun trailDao(): TrailDao?

    companion object {
        private var instance: TrailDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String?): TrailDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context!!, TrailDatabase::class.java, DB_NAME!!)
                    .build()
            }
            return instance
        }
    }
}
