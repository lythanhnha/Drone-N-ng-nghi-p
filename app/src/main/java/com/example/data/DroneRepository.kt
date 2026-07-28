package com.example.data

import kotlinx.coroutines.flow.Flow

class DroneRepository(
    private val farmerDao: FarmerDao,
    private val flightRecordDao: FlightRecordDao
) {
    val allFarmers: Flow<List<Farmer>> = farmerDao.getAllFarmers()
    val allFlightRecords: Flow<List<FlightRecord>> = flightRecordDao.getAllFlightRecords()

    fun getFarmerById(id: Long): Flow<Farmer?> = farmerDao.getFarmerById(id)

    fun getRecordsByFarmerId(farmerId: Long): Flow<List<FlightRecord>> =
        flightRecordDao.getRecordsByFarmerId(farmerId)

    suspend fun insertFarmer(farmer: Farmer): Long = farmerDao.insertFarmer(farmer)

    suspend fun updateFarmer(farmer: Farmer) = farmerDao.updateFarmer(farmer)

    suspend fun deleteFarmer(farmer: Farmer) {
        flightRecordDao.deleteRecordsByFarmerId(farmer.id)
        farmerDao.deleteFarmer(farmer)
    }

    suspend fun insertFlightRecord(record: FlightRecord): Long =
        flightRecordDao.insertFlightRecord(record)

    suspend fun updateFlightRecord(record: FlightRecord) =
        flightRecordDao.updateFlightRecord(record)

    suspend fun deleteFlightRecord(record: FlightRecord) =
        flightRecordDao.deleteFlightRecord(record)

    suspend fun recordPaymentForFarmer(farmerId: Long, paymentAmount: Double): Double {
        // Collect unpaid flight records for farmer from oldest to newest
        // Note: For background updates in suspend fun, we can query or process
        // We will fetch records and apply payment to remaining debts in order
        var remainingPayment = paymentAmount
        return remainingPayment
    }

    suspend fun updateFlightRecordPayment(recordId: Long, additionalPayment: Double) {
        val record = flightRecordDao.getRecordById(recordId) ?: return
        val newPaid = (record.paidAmount + additionalPayment).coerceAtMost(record.totalAmount)
        val isFullyPaid = newPaid >= record.totalAmount
        flightRecordDao.updateRecordPayment(recordId, newPaid, isFullyPaid)
    }
}
