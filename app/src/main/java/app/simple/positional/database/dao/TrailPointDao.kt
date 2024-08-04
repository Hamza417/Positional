package app.simple.positional.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.simple.positional.model.TrailPoint

@Dao
interface TrailPointDao {

    @Query("SELECT * FROM trail_data ORDER BY time_added COLLATE nocase")
    fun getAllTrailData(): List<TrailPoint>

    @Query("SELECT * FROM trail_data ORDER BY time_added COLLATE nocase DESC")
    fun getAllTrailDataDesc(): List<TrailPoint>

    /**
     * Insert and save trail data to Database
     *
     * @param trailPoint trail data details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrailData(trailPoint: TrailPoint)

    /**
     * Insert and save trail data list to Database
     *
     * @param trailData trail data details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrailData(trailData: List<TrailPoint>)

    /**
     * Update trail data
     *
     * @param trailPoint that will be updated
     */
    @Update
    suspend fun updateTrailData(trailPoint: TrailPoint)

    /**
     * @param trailPoint removes a location from the list
     */
    @Delete
    suspend fun deleteTrailData(trailPoint: TrailPoint)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM trail_data")
    fun nukeTable()
}
