package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlightRecord
import com.example.ui.components.HeaderBanner
import com.example.ui.theme.*
import com.example.ui.util.Formatters
import com.example.viewmodel.DashboardStats

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    recentRecords: List<FlightRecord>,
    onNavigateToRecordFlight: () -> Unit,
    onNavigateToFarmers: () -> Unit,
    onNavigateToDebtReport: () -> Unit,
    onDeleteRecord: (FlightRecord) -> Unit
) {
    var selectedRecordForDetail by remember { mutableStateOf<FlightRecord?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header Banner
        item {
            HeaderBanner(
                title = "Drone Nông Nghiệp",
                subtitle = "Quản lý doanh thu & công nợ chủ hộ"
            )
        }

        // 4 Core Stat Cards Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Tổng Doanh Thu",
                        value = Formatters.formatVnd(stats.totalRevenue),
                        icon = Icons.Default.Payments,
                        accentColor = PrimaryGreen,
                        valueColor = PrimaryGreen
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Tổng Bình Phun",
                        value = "${Formatters.formatNumber(stats.totalTanks)} bình",
                        icon = Icons.Default.WaterDrop,
                        accentColor = MintTeal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Khách Còn Nợ",
                        value = Formatters.formatVnd(stats.totalDebt),
                        icon = Icons.Default.Warning,
                        accentColor = if (stats.totalDebt > 0) DebtRed else PrimaryGreen,
                        valueColor = if (stats.totalDebt > 0) DebtRed else MaterialTheme.colorScheme.onSurface,
                        onClick = onNavigateToDebtReport
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Đã Thu Tiền",
                        value = Formatters.formatVnd(stats.totalPaid),
                        icon = Icons.Default.CheckCircle,
                        accentColor = PaidGreen,
                        valueColor = PaidGreen
                    )
                }
            }
        }

        // Quick Action Buttons Row
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Thao tác nhanh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "+ Chuyến bay",
                        icon = Icons.Default.FlightLand,
                        color = PrimaryGreen,
                        onClick = onNavigateToRecordFlight
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Quản lý chủ hộ",
                        icon = Icons.Default.People,
                        color = MintTeal,
                        onClick = onNavigateToFarmers
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Thu công nợ",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = DebtAmber,
                        onClick = onNavigateToDebtReport
                    )
                }
            }
        }

        // Doanh Thu Theo Loại Cây Trồng
        if (stats.cropDistributionMap.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Doanh thu theo cây trồng",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val maxCropRevenue = stats.cropDistributionMap.values.maxOrNull() ?: 1.0

                        stats.cropDistributionMap.forEach { (crop, revenue) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = crop,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = Formatters.formatVnd(revenue),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (revenue / maxCropRevenue).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Lịch Sử Bay Gần Đây Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lịch sử chuyến bay gần đây",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recentRecords.size} chuyến",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Recent Flight Records List
        if (recentRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightLand,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có dữ liệu chuyến bay",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(recentRecords.take(10)) { record ->
                FlightRecordCard(
                    record = record,
                    onClick = { selectedRecordForDetail = record },
                    onDelete = { onDeleteRecord(record) }
                )
            }
        }
    }

    // Detail Dialog when a flight record is clicked
    selectedRecordForDetail?.let { record ->
        FlightRecordDetailDialog(
            record = record,
            onDismiss = { selectedRecordForDetail = null },
            onDelete = {
                onDeleteRecord(record)
                selectedRecordForDetail = null
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    valueColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FlightRecordCard(
    record: FlightRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (record.isFullyPaid) PaidGreen.copy(alpha = 0.15f) else DebtRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (record.isFullyPaid) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = null,
                            tint = if (record.isFullyPaid) PaidGreen else DebtRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = record.farmerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${record.cropType} • ${Formatters.formatNumber(record.tankCount)} bình",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Text(
                        text = Formatters.formatDate(record.dateMillis),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = Formatters.formatVnd(record.totalAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (record.isFullyPaid) PaidGreen.copy(alpha = 0.12f) else DebtRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (record.isFullyPaid) "Đã xong" else "Nợ: ${Formatters.formatVnd(record.remainingDebt)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (record.isFullyPaid) PaidGreen else DebtRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlightRecordDetailDialog(
    record: FlightRecord,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Flight,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Chi tiết chuyến bay", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailItem(label = "Chủ hộ", value = record.farmerName)
                DetailItem(label = "Ngày phun", value = Formatters.formatDate(record.dateMillis))
                DetailItem(label = "Loại cây trồng", value = record.cropType)
                DetailItem(label = "Số bình phun", value = "${Formatters.formatNumber(record.tankCount)} bình")
                DetailItem(label = "Đơn giá/bình", value = Formatters.formatVnd(record.unitPrice))
                if (record.servicePesticideFee > 0) {
                    DetailItem(
                        label = "Phụ thu (${record.pesticideType.ifBlank { "Thuốc/DV" }})",
                        value = Formatters.formatVnd(record.servicePesticideFee)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DetailItem(label = "Tổng thành tiền", value = Formatters.formatVnd(record.totalAmount), isBold = true)
                DetailItem(label = "Đã thanh toán", value = Formatters.formatVnd(record.paidAmount))
                DetailItem(
                    label = "Còn nợ",
                    value = Formatters.formatVnd(record.remainingDebt),
                    valueColor = if (record.remainingDebt > 0) DebtRed else PaidGreen,
                    isBold = true
                )
                if (record.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ghi chú: ${record.notes}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Xóa chuyến bay", color = DebtRed)
            }
        }
    )
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
