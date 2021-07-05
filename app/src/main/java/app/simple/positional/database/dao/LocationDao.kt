package app.simple.positional.database.dao

import androidx.room.*
import app.simple.positional.model.Locations

@Dao
interface LocationDao {

    @Query("SELECT * FROM location ORDER BY date_added COLLATE nocase DESC LIMIT 99")
    fun getAllLocations(): MutableList<Locations>

    /**
     * Insert and save location to Database
     *
     * @param location saves location details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insetLocation(location: Locations)

    /**
     * Update location
     *
     * @param location that will be update
     */
    @Update
    suspend fun updateSong(location: Locations)

    /**
     * @param location removes a location from the list
     */
    @Delete
    suspend fun deleteLocation(location: Locations)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM location")
    fun nukeTable()
}