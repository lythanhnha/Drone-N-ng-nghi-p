package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.util.Formatters
import com.example.viewmodel.FarmerWithSummary

@Composable
fun DebtReportScreen(
    debtorsList: List<FarmerWithSummary>,
    onPayFarmerDebt: (farmerId: Long, amount: Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDebtorForPayment by remember { mutableStateOf<FarmerWithSummary?>(null) }

    val filteredDebtors = remember(debtorsList, searchQuery) {
        if (searchQuery.isBlank()) debtorsList
        else debtorsList.filter {
            it.farmer.name.contains(searchQuery, ignoreCase = true) ||
            it.farmer.phone.contains(searchQuery, ignoreCase = true) ||
            it.farmer.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalDebtAll = debtorsList.sumOf { it.totalDebt }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = DebtAmber,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Báo Cáo Công Nợ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Danh sách chủ hộ còn nợ & cập nhật thu nợ",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Total Debt Card inside Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TỔNG NỢ CẦN THU", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text(
                                text = Formatters.formatVnd(totalDebtAll),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DebtRed
                            )
                        }

                        Surface(
                            color = DebtRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "${debtorsList.size} chủ hộ còn nợ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DebtRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm tên chủ hộ còn nợ...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DebtAmber) },
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        // List of Debtors
        if (filteredDebtors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = PaidGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Không có công nợ! Tất cả chủ hộ đã thanh toán." else "Không tìm thấy kết quả nợ phù hợp.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDebtors) { debtor ->
                    DebtorCard(
                        summary = debtor,
                        onPayClick = { selectedDebtorForPayment = debtor }
                    )
                }
            }
        }
    }

    // Payment Dialog
    selectedDebtorForPayment?.let { debtor ->
        PayDebtDialog(
            maxDebt = debtor.totalDebt,
            farmerName = debtor.farmer.name,
            onDismiss = { selectedDebtorForPayment = null },
            onConfirmPay = { amount ->
                onPayFarmerDebt(debtor.farmer.id, amount)
                selectedDebtorForPayment = null
            }
        )
    }
}

@Composable
fun DebtorCard(
    summary: FarmerWithSummary,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        color = DebtRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DebtRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = summary.farmer.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = summary.farmer.phone.ifBlank { "Chưa có SĐT" },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DebtAmber),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trả nợ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tổng chi dịch vụ", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = Formatters.formatVnd(summary.totalSpent),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text("Đã trả", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = Formatters.formatVnd(summary.totalPaid),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PaidGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Số tiền còn nợ", fontSize = 11.sp, color = DebtRed, fontWeight = FontWeight.Bold)
                        Text(
                            text = Formatters.formatVnd(summary.totalDebt),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DebtRed
                        )
                    }
                }
            }
        }
    }
}
