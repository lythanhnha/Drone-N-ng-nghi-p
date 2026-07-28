package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.util.Formatters
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class FarmerWithSummary(
    val farmer: Farmer,
    val totalSpent: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDebt: Double = 0.0,
    val flightCount: Int = 0,
    val lastFlightDateMillis: Long? = null
)

data class DashboardStats(
    val totalRevenue: Double = 0.0,
    val totalTanks: Double = 0.0,
    val totalDebt: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalJobsCount: Int = 0,
    val monthlyRevenueMap: Map<String, Double> = emptyMap(),
    val cropDistributionMap: Map<String, Double> = emptyMap()
)

class DroneViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DroneRepository = DroneRepository(
        AppDatabase.getDatabase(application).farmerDao(),
        AppDatabase.getDatabase(application).flightRecordDao()
    )

    val farmers: StateFlow<List<Farmer>> = repository.allFarmers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flightRecords: StateFlow<List<FlightRecord>> = repository.allFlightRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Farmers with financial summaries
    val farmersWithSummary: StateFlow<List<FarmerWithSummary>> = combine(farmers, flightRecords) { farmerList, records ->
        farmerList.map { farmer ->
            val farmerRecords = records.filter { it.farmerId == farmer.id }
            val totalSpent = farmerRecords.sumOf { it.totalAmount }
            val totalPaid = farmerRecords.sumOf { it.paidAmount }
            val totalDebt = farmerRecords.sumOf { it.remainingDebt }
            val lastFlight = farmerRecords.maxByOrNull { it.dateMillis }?.dateMillis
            FarmerWithSummary(
                farmer = farmer,
                totalSpent = totalSpent,
                totalPaid = totalPaid,
                totalDebt = totalDebt,
                flightCount = farmerRecords.size,
                lastFlightDateMillis = lastFlight
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics Calculation
    val dashboardStats: StateFlow<DashboardStats> = flightRecords.map { records ->
        val totalRevenue = records.sumOf { it.totalAmount }
        val totalTanks = records.sumOf { it.tankCount }
        val totalDebt = records.sumOf { it.remainingDebt }
        val totalPaid = records.sumOf { it.paidAmount }
        
        // Monthly breakdown
        val monthlyMap = mutableMapOf<String, Double>()
        val cropMap = mutableMapOf<String, Double>()

        records.forEach { record ->
            val monthKey = Formatters.formatMonthYear(record.dateMillis)
            monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0.0) + record.totalAmount
            
            val cropKey = record.cropType.ifBlank { "Khác" }
            cropMap[cropKey] = (cropMap[cropKey] ?: 0.0) + record.totalAmount
        }

        DashboardStats(
            totalRevenue = totalRevenue,
            totalTanks = totalTanks,
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            totalJobsCount = records.size,
            monthlyRevenueMap = monthlyMap,
            cropDistributionMap = cropMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // List of farmers who currently have outstanding debt
    val debtorsList: StateFlow<List<FarmerWithSummary>> = farmersWithSummary.map { list ->
        list.filter { it.totalDebt > 0 }.sortedByDescending { it.totalDebt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed mock data if database is empty on first startup
        viewModelScope.launch {
            checkAndSeedData()
        }
    }

    // Farmer Operations
    fun addFarmer(name: String, phone: String, address: String, notes: String) {
        viewModelScope.launch {
            val newFarmer = Farmer(name = name, phone = phone, address = address, notes = notes)
            repository.insertFarmer(newFarmer)
        }
    }

    fun updateFarmer(farmer: Farmer) {
        viewModelScope.launch {
            repository.updateFarmer(farmer)
        }
    }

    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            repository.deleteFarmer(farmer)
        }
    }

    // Flight Record Operations
    fun addFlightRecord(
        farmerId: Long,
        farmerName: String,
        dateMillis: Long,
        cropType: String,
        tankCount: Double,
        unitPrice: Double,
        servicePesticideFee: Double,
        pesticideType: String,
        paidAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val totalAmount = (tankCount * unitPrice) + servicePesticideFee
            val isFullyPaid = paidAmount >= totalAmount
            val record = FlightRecord(
                farmerId = farmerId,
                farmerName = farmerName,
                dateMillis = dateMillis,
                cropType = cropType,
                tankCount = tankCount,
                unitPrice = unitPrice,
                servicePesticideFee = servicePesticideFee,
                pesticideType = pesticideType,
                totalAmount = totalAmount,
                paidAmount = paidAmount.coerceAtMost(totalAmount),
                isFullyPaid = isFullyPaid,
                notes = notes
            )
            repository.insertFlightRecord(record)
        }
    }

    fun updateFlightRecordPayment(recordId: Long, additionalPayment: Double) {
        viewModelScope.launch {
            repository.updateFlightRecordPayment(recordId, additionalPayment)
        }
    }

    fun payFarmerDebt(farmerId: Long, paymentAmount: Double) {
        viewModelScope.launch {
            val records = flightRecords.value
                .filter { it.farmerId == farmerId && !it.isFullyPaid }
                .sortedBy { it.dateMillis } // Pay oldest unpaid records first

            var remainingToPay = paymentAmount
            for (record in records) {
                if (remainingToPay <= 0) break
                val debt = record.remainingDebt
                val payForThisRecord = remainingToPay.coerceAtMost(debt)
                val newPaidAmount = record.paidAmount + payForThisRecord
                val isFully = newPaidAmount >= record.totalAmount
                
                repository.updateFlightRecord(
                    record.copy(
                        paidAmount = newPaidAmount,
                        isFullyPaid = isFully
                    )
                )
                remainingToPay -= payForThisRecord
            }
        }
    }

    fun deleteFlightRecord(record: FlightRecord) {
        viewModelScope.launch {
            repository.deleteFlightRecord(record)
        }
    }

    // Seed realistic Vietnamese farmer data if empty
    private suspend fun checkAndSeedData() {
        if (repository.allFarmers.first().isNotEmpty()) return

        val farmer1Id = repository.insertFarmer(Farmer(name = "Chú Hai Lúa", phone = "0912345678", address = "Cánh đồng Tân An, An Giang", notes = "Ruộng lúa 20 mẫu"))
        val farmer2Id = repository.insertFarmer(Farmer(name = "Bác Bảy Sầu Riêng", phone = "0987654321", address = "Vườn Cái Bè, Tiền Giang", notes = "Vườn sầu riêng 15 mẫu"))
        val farmer3Id = repository.insertFarmer(Farmer(name = "Anh Minh Cà Phê", phone = "0909112233", address = "Xã Cư M'gar, Đắk Lắk", notes = "Đồi cà phê 12 mẫu"))
        val farmer4Id = repository.insertFarmer(Farmer(name = "Cô Năm Rau Màu", phone = "0933445566", address = "Huyện Đức Trọng, Lâm Đồng", notes = "Trang trại rau hoa"))

        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        // Recent flights
        repository.insertFlightRecord(
            FlightRecord(
                farmerId = farmer1Id,
                farmerName = "Chú Hai Lúa",
                dateMillis = now - (1 * dayMillis),
                cropType = "Lúa",
                tankCount = 18.0,
                unitPrice = 160000.0,
                servicePesticideFee = 100000.0,
                pesticideType = "Thuốc trị bệnh đạo ôn",
                totalAmount = 18.0 * 160000.0 + 100000.0,
                paidAmount = 2980000.0,
                isFullyPaid = true,
                notes = "Phun xịt đợt 2 lúa trổ bông"
            )
        )

        repository.insertFlightRecord(
            FlightRecord(
                farmerId = farmer2Id,
                farmerName = "Bác Bảy Sầu Riêng",
                dateMillis = now - (3 * dayMillis),
                cropType = "Sầu Riêng",
                tankCount = 25.0,
                unitPrice = 200000.0,
                servicePesticideFee = 250000.0,
                pesticideType = "Phun phân bón lá & Dưỡng trái",
                totalAmount = 25.0 * 200000.0 + 250000.0,
                paidAmount = 3000000.0,
                isFullyPaid = false,
                notes = "Còn nợ 2.250.000 đ hứa trả tuần sau"
            )
        )

        repository.insertFlightRecord(
            FlightRecord(
                farmerId = farmer3Id,
                farmerName = "Anh Minh Cà Phê",
                dateMillis = now - (7 * dayMillis),
                cropType = "Cà Phê",
                tankCount = 15.0,
                unitPrice = 180000.0,
                servicePesticideFee = 150000.0,
                pesticideType = "Thuốc trừ rệp sáp",
                totalAmount = 15.0 * 180000.0 + 150000.0,
                paidAmount = 1000000.0,
                isFullyPaid = false,
                notes = "Chưa trả đủ, nợ 1.850.000 đ"
            )
        )

        repository.insertFlightRecord(
            FlightRecord(
                farmerId = farmer4Id,
                farmerName = "Cô Năm Rau Màu",
                dateMillis = now - (12 * dayMillis),
                cropType = "Rau Màu",
                tankCount = 10.0,
                unitPrice = 150000.0,
                servicePesticideFee = 50000.0,
                pesticideType = "Phun vi sinh phòng trừ sâu bệnh",
                totalAmount = 10.0 * 150000.0 + 50000.0,
                paidAmount = 1550000.0,
                isFullyPaid = true,
                notes = "Đã thanh toán bằng chuyển khoản"
            )
        )
    }
}

class DroneViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DroneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DroneViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
