package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightRecordDao {
    @Query("SELECT * FROM flight_records ORDER BY dateMillis DESC")
    fun getAllFlightRecords(): Flow<List<FlightRecord>>

    @Query("SELECT * FROM flight_records WHERE farmerId = :farmerId ORDER BY dateMillis DESC")
    fun getRecordsByFarmerId(farmerId: Long): Flow<List<FlightRecord>>

    @Query("SELECT * FROM flight_records WHERE id = :id")
    suspend fun getRecordById(id: Long): FlightRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlightRecord(record: FlightRecord): Long

    @Update
    suspend fun updateFlightRecord(record: FlightRecord)

    @Delete
    suspend fun deleteFlightRecord(record: FlightRecord)

    @Query("DELETE FROM flight_records WHERE farmerId = :farmerId")
    suspend fun deleteRecordsByFarmerId(farmerId: Long)

    @Query("UPDATE flight_records SET paidAmount = :newPaidAmount, isFullyPaid = :isFullyPaid WHERE id = :recordId")
    suspend fun updateRecordPayment(recordId: Long, newPaidAmount: Double, isFullyPaid: Boolean)
}
