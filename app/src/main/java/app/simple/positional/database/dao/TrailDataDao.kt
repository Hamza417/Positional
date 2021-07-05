package app.simple.positional.database.dao

import androidx.room.*
import app.simple.positional.model.TrailData

@Dao
interface TrailDataDao {

    @Query("SELECT * FROM trail_data ORDER BY time_added COLLATE nocase")
    fun getAllTrailData(): List<TrailData>

    /**
     * Insert and save trail data to Database
     *
     * @param trailData trail data details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrailData(trailData: TrailData)

    /**
     * Insert and save trail data list to Database
     *
     * @param trailData trail data details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrailData(trailData: List<TrailData>)

    /**
     * Update trail data
     *
     * @param trailData that will be updated
     */
    @Update
    suspend fun updateTrailData(trailData: TrailData)

    /**
     * @param trailData removes a location from the list
     */
    @Delete
    suspend fun deleteTrailData(trailData: TrailData)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM trail_data")
    fun nukeTable()
}