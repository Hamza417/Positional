package app.simple.positional.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.simple.positional.model.TrailEntry

@Dao
interface TrailDao {

    @Query("SELECT * FROM trails ORDER BY date_added COLLATE nocase DESC")
    fun getAllTrails(): MutableList<TrailEntry>

    /**
     * Insert and save location to Database
     *
     * @param trailEntry saves location details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trailEntry: TrailEntry)

    /**
     * Update trail
     *
     * @param trailEntry that will be update
     */
    @Update
    suspend fun updateTrail(trailEntry: TrailEntry)

    /**
     * @param trailEntry removes a location from the list
     */
    @Delete
    suspend fun deleteTrail(trailEntry: TrailEntry)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM trails")
    fun nukeTable()
}
