package app.simple.positional.database.dao

import androidx.room.*
import app.simple.positional.model.TrailModel

@Dao
interface TrailDao {

    @Query("SELECT * FROM trails ORDER BY date_added COLLATE nocase")
    fun getAllTrails(): MutableList<TrailModel>

    /**
     * Insert and save location to Database
     *
     * @param trailModel saves location details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trailModel: TrailModel)

    /**
     * Update trail
     *
     * @param trailModel that will be update
     */
    @Update
    suspend fun updateTrail(trailModel: TrailModel)

    /**
     * @param trailModel removes a location from the list
     */
    @Delete
    suspend fun deleteTrail(trailModel: TrailModel)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM trails")
    fun nukeTable()
}