package app.simple.positional.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.simple.positional.model.MeasurePoint

@Dao
interface MeasurePointsDao {

    @Query("SELECT * FROM measure_points_data ORDER BY time_added COLLATE nocase DESC")
    fun getAllMeasurePoints(): List<MeasurePoint>

    @Insert
    suspend fun insertMeasurePoint(measurePoint: MeasurePoint)

    @Update
    suspend fun updateMeasurePoint(measurePoint: MeasurePoint)

    @Delete
    suspend fun deleteMeasurePoint(measurePoint: MeasurePoint)

    @Query("DELETE FROM measure_points_data")
    fun nukeTable()
}
