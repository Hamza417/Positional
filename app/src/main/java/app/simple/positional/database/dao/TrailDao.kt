package app.simple.positional.database.dao

import androidx.room.*
import app.simple.positional.model.Trails

@Dao
interface TrailDao {

    @Query("SELECT * FROM Trails ORDER BY date_added COLLATE nocase DESC LIMIT 99")
    fun getAllLocations(): MutableList<Trails>

    /**
     * Insert and save location to Database
     *
     * @param trails saves location details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trails: Trails)

    /**
     * Update trail
     *
     * @param trails that will be update
     */
    @Update
    suspend fun updateTrail(trails: Trails)

    /**
     * @param trails removes a location from the list
     */
    @Delete
    suspend fun deleteTrail(trails: Trails)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM trails")
    fun nukeTable()
}