package app.simple.positional.database.dao

import androidx.room.*
import app.simple.positional.model.DirectionModel

@Dao
interface DirectionDao {
    @Query("SELECT * FROM directions ORDER BY date_added COLLATE nocase DESC LIMIT 999")
    fun getAllDirections(): MutableList<DirectionModel>

    /**
     * Insert and save direction to Database
     *
     * @param direction saves direction details
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirection(direction: DirectionModel)

    /**
     * Update direction
     *
     * @param direction that will be update
     */
    @Update
    suspend fun updateDirection(direction: DirectionModel)

    /**
     * @param direction removes a direction from the list
     */
    @Delete
    suspend fun deleteDirection(direction: DirectionModel)

    /**
     * Deletes the entire database, possibly to create a new one
     */
    @Query("DELETE FROM directions")
    fun nukeTable()
}