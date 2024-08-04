package app.simple.positional.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.simple.positional.model.MeasureEntry

@Dao
interface MeasureDao {

    @Query("SELECT * FROM measures ORDER BY date_added COLLATE nocase DESC")
    fun getAllMeasures(): MutableList<MeasureEntry>

    @Delete
    suspend fun deleteMeasure(measureEntry: MeasureEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasure(measureEntry: MeasureEntry)

    @Update
    suspend fun updateMeasure(measureEntry: MeasureEntry)

    @Query("DELETE FROM measures")
    fun nukeTable()

}
