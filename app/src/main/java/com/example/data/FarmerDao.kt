package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY name ASC")
    fun getAllFarmers(): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE id = :id")
    fun getFarmerById(id: Long): Flow<Farmer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: Farmer): Long

    @Update
    suspend fun updateFarmer(farmer: Farmer)

    @Delete
    suspend fun deleteFarmer(farmer: Farmer)

    @Query("DELETE FROM farmers WHERE id = :id")
    suspend fun deleteFarmerById(id: Long)
}
