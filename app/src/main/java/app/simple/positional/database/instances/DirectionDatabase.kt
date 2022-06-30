package app.simple.positional.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.positional.database.dao.DirectionDao
import app.simple.positional.model.DirectionModel

@Database(entities = [DirectionModel::class], exportSchema = true, version = 1)
abstract class DirectionDatabase : RoomDatabase() {

    abstract fun directionDao(): DirectionDao?

    companion object {
        private var instance: DirectionDatabase? = null

        @Synchronized
        fun getInstance(context: Context?, DB_NAME: String? = "directions"): DirectionDatabase? {
            if (instance == null || !instance!!.isOpen) {
                instance = Room.databaseBuilder(context!!, DirectionDatabase::class.java, DB_NAME!!)
                        .fallbackToDestructiveMigration()
                        .build()
            }

            return instance
        }

        fun close() {
            instance?.close()
            instance = null
        }
    }
}