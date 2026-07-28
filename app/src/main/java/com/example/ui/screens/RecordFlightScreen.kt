package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Farmer
import com.example.ui.theme.*
import com.example.ui.util.Formatters
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFlightScreen(
    farmers: List<Farmer>,
    onSaveRecord: (
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
    ) -> Unit,
    onAddNewFarmerRequested: () -> Unit,
    onSuccessSaved: () -> Unit
) {
    val context = LocalContext.current

    // State
    var selectedFarmer by remember { mutableStateOf<Farmer?>(farmers.firstOrNull()) }
    var isFarmerDropdownExpanded by remember { mutableStateOf(false) }

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val cropTypes = listOf("Lúa", "Sầu Riêng", "Cà Phê", "Cây Ăn Trái", "Rau Màu", "Bắp/Ngô", "Khác")
    var selectedCropType by remember { mutableStateOf(cropTypes[0]) }

    var tankCountText by remember { mutableStateOf("10") }
    var unitPriceText by remember { mutableStateOf("160000") }
    var serviceFeeText by remember { mutableStateOf("0") }
    var pesticideTypeText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    // Payment state options: FULL_PAID, PARTIAL_PAID, UNPAID
    var paymentOption by remember { mutableStateOf("FULL_PAID") }
    var partialPaidText by remember { mutableStateOf("") }

    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculations
    val tankCount = tankCountText.toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val serviceFee = serviceFeeText.toDoubleOrNull() ?: 0.0
    val totalAmount = (tankCount * unitPrice) + serviceFee

    val actualPaidAmount = when (paymentOption) {
        "FULL_PAID" -> totalAmount
        "UNPAID" -> 0.0
        "PARTIAL_PAID" -> (partialPaidText.toDoubleOrNull() ?: 0.0).coerceAtMost(totalAmount)
        else -> totalAmount
    }

    val dateCalendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDateMillis = cal.timeInMillis
            },
            dateCalendar.get(Calendar.YEAR),
            dateCalendar.get(Calendar.MONTH),
            dateCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        snackbarHost = {
            if (showSuccessSnackbar) {
                Snackbar(
                    containerColor = PaidGreen,
                    contentColor = Color.White,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Lưu chuyến bay thành công!", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Surface(
                color = PrimaryGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Ghi Nhận Chuyến Bay",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Nhập thông tin diện tích/số bình, đơn giá và thu tiền",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. SELECT FARMER
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Chọn Chủ Hộ *",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            TextButton(onClick = onAddNewFarmerRequested) {
                                Text("+ Thêm chủ hộ mới", fontSize = 12.sp, color = SecondaryGreen)
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = isFarmerDropdownExpanded,
                            onExpandedChange = { isFarmerDropdownExpanded = !isFarmerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedFarmer?.name ?: "Chưa chọn chủ hộ",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Chủ hộ") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFarmerDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = isFarmerDropdownExpanded,
                                onDismissRequest = { isFarmerDropdownExpanded = false }
                            ) {
                                if (farmers.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Chưa có chủ hộ. Nhấn để tạo mới.") },
                                        onClick = {
                                            isFarmerDropdownExpanded = false
                                            onAddNewFarmerRequested()
                                        }
                                    )
                                } else {
                                    farmers.forEach { farmer ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(farmer.name, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = "${farmer.phone} - ${farmer.address}",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedFarmer = farmer
                                                isFarmerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. FLIGHT DETAILS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "2. Thông Tin Phun Thuốc",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        // Date Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Ngày phun", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = Formatters.formatDate(selectedDateMillis),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text("Đổi ngày", fontSize = 12.sp, color = SecondaryGreen, fontWeight = FontWeight.Bold)
                        }

                        // Crop Type Chips
                        Text("Loại cây trồng:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cropTypes.take(4).forEach { crop ->
                                FilterChip(
                                    selected = selectedCropType == crop,
                                    onClick = { selectedCropType = crop },
                                    label = { Text(crop, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cropTypes.drop(4).forEach { crop ->
                                FilterChip(
                                    selected = selectedCropType == crop,
                                    onClick = { selectedCropType = crop },
                                    label = { Text(crop, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Inputs: Tank Count & Unit Price
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = tankCountText,
                                onValueChange = { tankCountText = it },
                                label = { Text("Số bình / Mẫu") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = unitPriceText,
                                onValueChange = { unitPriceText = it },
                                label = { Text("Đơn giá (VND/bình)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Optional Pesticide Fee & Description
                        OutlinedTextField(
                            value = serviceFeeText,
                            onValueChange = { serviceFeeText = it },
                            label = { Text("Phụ thu thuốc/dịch vụ (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (serviceFee > 0) {
                            OutlinedTextField(
                                value = pesticideTypeText,
                                onValueChange = { pesticideTypeText = it },
                                label = { Text("Tên thuốc / loại phụ thu") },
                                placeholder = { Text("Ví dụ: Thuốc trừ sâu rốt, dưỡng lá...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // 3. CALCULATION & PAYMENT STATUS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "3. Tổng Tiền & Thanh Toán",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        // Live Total Amount Calculation Banner
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tổng Thành Tiền", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = Formatters.formatVnd(totalAmount),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }

                                Surface(
                                    color = PrimaryGreen.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${Formatters.formatNumber(tankCount)} bình x ${Formatters.formatVnd(unitPrice)}",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text("Trạng thái thu tiền:", fontSize = 13.sp, fontWeight = FontWeight.Medium)

                        // Payment Status Radio/Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = paymentOption == "FULL_PAID",
                                    onClick = { paymentOption = "FULL_PAID" }
                                )
                                Text("Thanh toán đủ ngay (${Formatters.formatVnd(totalAmount)})", fontSize = 13.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = paymentOption == "PARTIAL_PAID",
                                    onClick = { paymentOption = "PARTIAL_PAID" }
                                )
                                Text("Thanh toán một phần (Trả trước)", fontSize = 13.sp)
                            }

                            if (paymentOption == "PARTIAL_PAID") {
                                OutlinedTextField(
                                    value = partialPaidText,
                                    onValueChange = { partialPaidText = it },
                                    label = { Text("Số tiền đã trả trước (VND)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 32.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = paymentOption == "UNPAID",
                                    onClick = { paymentOption = "UNPAID" }
                                )
                                Text("Ghi nợ toàn bộ", fontSize = 13.sp, color = DebtRed, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Notes Input
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Ghi chú chuyến bay (Tùy chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = DebtRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // SAVE BUTTON
                Button(
                    onClick = {
                        val farmer = selectedFarmer
                        if (farmer == null) {
                            errorMessage = "Vui lòng chọn hoặc thêm chủ hộ trước!"
                            return@Button
                        }
                        if (tankCount <= 0 || unitPrice <= 0) {
                            errorMessage = "Số bình và đơn giá phải lớn hơn 0!"
                            return@Button
                        }

                        errorMessage = null
                        onSaveRecord(
                            farmer.id,
                            farmer.name,
                            selectedDateMillis,
                            selectedCropType,
                            tankCount,
                            unitPrice,
                            serviceFee,
                            pesticideTypeText.trim(),
                            actualPaidAmount,
                            notesText.trim()
                        )

                        showSuccessSnackbar = true
                        onSuccessSaved()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu Chuyến Bay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
