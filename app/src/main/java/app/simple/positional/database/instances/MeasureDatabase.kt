package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.database.dao.MeasureDao
import app.simple.positional.model.MeasureEntry

@Database(entities = [MeasureEntry::class], exportSchema = true, version = 1)
abstract class MeasureDatabase : RoomDatabase() {

    abstract fun measureDao(): MeasureDao?

    companion object {
        private var instance: MeasureDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String?): MeasureDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context!!, MeasureDatabase::class.java, DB_NAME!!)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}
