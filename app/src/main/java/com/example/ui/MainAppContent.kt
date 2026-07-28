package com.example.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.DroneViewModel
import com.example.viewmodel.DroneViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: DroneViewModel = viewModel(
        factory = DroneViewModelFactory(application)
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    val farmers by viewModel.farmers.collectAsStateWithLifecycle()
    val farmersWithSummary by viewModel.farmersWithSummary.collectAsStateWithLifecycle()
    val flightRecords by viewModel.flightRecords.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val debtorsList by viewModel.debtorsList.collectAsStateWithLifecycle()

    var showQuickAddFarmerModal by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Tổng Quan") },
                    label = { Text("Tổng quan", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Chủ Hộ") },
                    label = { Text("Chủ hộ", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.FlightTakeoff, contentDescription = "Ghi Lịch Bay") },
                    label = { Text("Ghi lịch bay", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (stats.totalDebt > 0) {
                                    Badge(containerColor = DebtRed, contentColor = Color.White) {
                                        Text("${debtorsList.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Công Nợ")
                        }
                    },
                    label = { Text("Báo cáo nợ", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.tertiary,
                        selectedTextColor = MaterialTheme.colorScheme.tertiary,
                        indicatorColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    stats = stats,
                    recentRecords = flightRecords,
                    onNavigateToRecordFlight = { selectedTab = 2 },
                    onNavigateToFarmers = { selectedTab = 1 },
                    onNavigateToDebtReport = { selectedTab = 3 },
                    onDeleteRecord = { record -> viewModel.deleteFlightRecord(record) }
                )

                1 -> FarmerManagementScreen(
                    farmersWithSummary = farmersWithSummary,
                    flightRecords = flightRecords,
                    onAddFarmer = { name, phone, address, notes ->
                        viewModel.addFarmer(name, phone, address, notes)
                    },
                    onUpdateFarmer = { farmer -> viewModel.updateFarmer(farmer) },
                    onDeleteFarmer = { farmer -> viewModel.deleteFarmer(farmer) },
                    onPayFarmerDebt = { farmerId, amount ->
                        viewModel.payFarmerDebt(farmerId, amount)
                    }
                )

                2 -> RecordFlightScreen(
                    farmers = farmers,
                    onSaveRecord = { farmerId, farmerName, dateMillis, cropType, tankCount, unitPrice, serviceFee, pesticideType, paidAmount, notes ->
                        viewModel.addFlightRecord(
                            farmerId, farmerName, dateMillis, cropType, tankCount, unitPrice, serviceFee, pesticideType, paidAmount, notes
                        )
                    },
                    onAddNewFarmerRequested = { showQuickAddFarmerModal = true },
                    onSuccessSaved = { selectedTab = 0 }
                )

                3 -> DebtReportScreen(
                    debtorsList = debtorsList,
                    onPayFarmerDebt = { farmerId, amount ->
                        viewModel.payFarmerDebt(farmerId, amount)
                    }
                )
            }
        }
    }

    // Modal Quick Add Farmer when requested from RecordFlightScreen
    if (showQuickAddFarmerModal) {
        AddEditFarmerDialog(
            farmer = null,
            onDismiss = { showQuickAddFarmerModal = false },
            onSave = { name, phone, address, notes ->
                viewModel.addFarmer(name, phone, address, notes)
                showQuickAddFarmerModal = false
            }
        )
    }
}
