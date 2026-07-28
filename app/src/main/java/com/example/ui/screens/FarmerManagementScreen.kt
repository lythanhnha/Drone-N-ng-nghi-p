package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Farmer
import com.example.data.FlightRecord
import com.example.ui.theme.*
import com.example.ui.util.Formatters
import com.example.viewmodel.FarmerWithSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerManagementScreen(
    farmersWithSummary: List<FarmerWithSummary>,
    flightRecords: List<FlightRecord>,
    onAddFarmer: (String, String, String, String) -> Unit,
    onUpdateFarmer: (Farmer) -> Unit,
    onDeleteFarmer: (Farmer) -> Unit,
    onPayFarmerDebt: (Long, Double) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddFarmerDialog by remember { mutableStateOf(false) }
    var selectedFarmerForDetail by remember { mutableStateOf<FarmerWithSummary?>(null) }
    var farmerToEdit by remember { mutableStateOf<Farmer?>(null) }

    val filteredFarmers = remember(farmersWithSummary, searchQuery) {
        if (searchQuery.isBlank()) {
            farmersWithSummary
        } else {
            farmersWithSummary.filter {
                it.farmer.name.contains(searchQuery, ignoreCase = true) ||
                it.farmer.phone.contains(searchQuery, ignoreCase = true) ||
                it.farmer.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddFarmerDialog = true },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Thêm chủ hộ", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Title & Search Bar
            Surface(
                color = PrimaryGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Danh Sách Chủ Hộ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Quản lý thông tin khách hàng, lịch sử phun & công nợ",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm theo tên, SĐT, khu vực...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = LightGreenAccent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Farmer List
            if (filteredFarmers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Chưa có danh sách chủ hộ" else "Không tìm thấy chủ hộ phù hợp",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredFarmers) { item ->
                        FarmerCard(
                            farmerSummary = item,
                            onClick = { selectedFarmerForDetail = item },
                            onCall = { makeCall(context, item.farmer.phone) },
                            onSms = { sendSms(context, item.farmer.phone) }
                        )
                    }
                }
            }
        }
    }

    // Dialog Add Farmer
    if (showAddFarmerDialog) {
        AddEditFarmerDialog(
            farmer = null,
            onDismiss = { showAddFarmerDialog = false },
            onSave = { name, phone, address, notes ->
                onAddFarmer(name, phone, address, notes)
                showAddFarmerDialog = false
            }
        )
    }

    // Dialog Edit Farmer
    farmerToEdit?.let { farmer ->
        AddEditFarmerDialog(
            farmer = farmer,
            onDismiss = { farmerToEdit = null },
            onSave = { name, phone, address, notes ->
                onUpdateFarmer(farmer.copy(name = name, phone = phone, address = address, notes = notes))
                farmerToEdit = null
            }
        )
    }

    // Sheet / Modal Farmer Detail with Flight History & Debt
    selectedFarmerForDetail?.let { summary ->
        FarmerDetailBottomSheet(
            summary = summary,
            flightRecords = flightRecords.filter { it.farmerId == summary.farmer.id },
            onDismiss = { selectedFarmerForDetail = null },
            onEdit = {
                farmerToEdit = summary.farmer
                selectedFarmerForDetail = null
            },
            onDelete = {
                onDeleteFarmer(summary.farmer)
                selectedFarmerForDetail = null
            },
            onPayDebt = { amount ->
                onPayFarmerDebt(summary.farmer.id, amount)
            }
        )
    }
}

@Composable
fun FarmerCard(
    farmerSummary: FarmerWithSummary,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onSms: () -> Unit
) {
    val farmer = farmerSummary.farmer
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryGreen.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = farmer.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = farmer.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = farmer.phone.ifBlank { "Chưa có SĐT" },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Gọi",
                            tint = PrimaryGreen
                        )
                    }
                    IconButton(onClick = onSms, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "Nhắn tin",
                            tint = MintTeal
                        )
                    }
                }
            }

            if (farmer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = farmer.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Số chuyến: ${farmerSummary.flightCount}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Tổng chi: ${Formatters.formatVnd(farmerSummary.totalSpent)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Còn nợ",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = Formatters.formatVnd(farmerSummary.totalDebt),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (farmerSummary.totalDebt > 0) DebtRed else PaidGreen
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditFarmerDialog(
    farmer: Farmer?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(farmer?.name ?: "") }
    var phone by remember { mutableStateOf(farmer?.phone ?: "") }
    var address by remember { mutableStateOf(farmer?.address ?: "") }
    var notes by remember { mutableStateOf(farmer?.notes ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (farmer == null) "Thêm Chủ Hộ Mới" else "Cập Nhật Chủ Hộ",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Tên chủ hộ *") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ / Khu vực cánh đồng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú (Loại cây, diện tích,...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onSave(name.trim(), phone.trim(), address.trim(), notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Lưu thông tin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDetailBottomSheet(
    summary: FarmerWithSummary,
    flightRecords: List<FlightRecord>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayDebt: (Double) -> Unit
) {
    val farmer = summary.farmer
    var showPayDebtDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = farmer.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = farmer.phone.ifBlank { "Chưa có SĐT" },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryGreen)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = DebtRed)
                    }
                }
            }

            if (farmer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Địa chỉ: ${farmer.address}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Debt Summary Card for Farmer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.totalDebt > 0) DebtRed.copy(alpha = 0.12f) else PaidGreen.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (summary.totalDebt > 0) "Dư nợ còn lại" else "Đã thanh toán đủ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (summary.totalDebt > 0) DebtRed else PaidGreen
                        )
                        Text(
                            text = Formatters.formatVnd(summary.totalDebt),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.totalDebt > 0) DebtRed else PaidGreen
                        )
                    }

                    if (summary.totalDebt > 0) {
                        Button(
                            onClick = { showPayDebtDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DebtAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Trả Nợ Nhanh", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lịch sử chuyến bay (${flightRecords.size} chuyến)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (flightRecords.isEmpty()) {
                Text(
                    text = "Chủ hộ này chưa có lịch sử chuyến bay.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(flightRecords) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${Formatters.formatDate(record.dateMillis)} - ${record.cropType}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${Formatters.formatNumber(record.tankCount)} bình x ${Formatters.formatVnd(record.unitPrice)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.formatVnd(record.totalAmount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                    Text(
                                        text = if (record.isFullyPaid) "Đã thanh toán" else "Nợ: ${Formatters.formatVnd(record.remainingDebt)}",
                                        fontSize = 11.sp,
                                        color = if (record.isFullyPaid) PaidGreen else DebtRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPayDebtDialog) {
        PayDebtDialog(
            maxDebt = summary.totalDebt,
            farmerName = farmer.name,
            onDismiss = { showPayDebtDialog = false },
            onConfirmPay = { amount ->
                onPayDebt(amount)
                showPayDebtDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun PayDebtDialog(
    maxDebt: Double,
    farmerName: String,
    onDismiss: () -> Unit,
    onConfirmPay: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(maxDebt.toLong().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cập Nhật Trả Nợ: $farmerName", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Tổng nợ hiện tại: ${Formatters.formatVnd(maxDebt)}",
                    fontSize = 14.sp,
                    color = DebtRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Số tiền khách trả (VND)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = amountText == maxDebt.toLong().toString(),
                        onClick = { amountText = maxDebt.toLong().toString() },
                        label = { Text("Trả hết nợ") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirmPay(amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DebtAmber)
            ) {
                Text("Xác nhận thu nợ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private fun makeCall(context: Context, phone: String) {
    if (phone.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    context.startActivity(intent)
}

private fun sendSms(context: Context, phone: String) {
    if (phone.isBlank()) return
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
    context.startActivity(intent)
}
