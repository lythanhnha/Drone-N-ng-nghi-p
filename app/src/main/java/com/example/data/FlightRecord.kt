package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flight_records")
data class FlightRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val farmerId: Long,
    val farmerName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val cropType: String, // Ví dụ: Lúa, Sầu riêng, Cà phê, Cây ăn trái, Rau màu
    val tankCount: Double, // Số bình / Số mẫu
    val unitPrice: Double, // Đơn giá (VND/bình)
    val servicePesticideFee: Double = 0.0, // Phụ thu thuốc hoặc dịch vụ khác
    val pesticideType: String = "", // Tên loại thuốc/dịch vụ phụ thu
    val totalAmount: Double, // Tổng tiền = tankCount * unitPrice + servicePesticideFee
    val paidAmount: Double, // Số tiền đã thanh toán
    val isFullyPaid: Boolean = paidAmount >= totalAmount,
    val notes: String = ""
) {
    val remainingDebt: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
