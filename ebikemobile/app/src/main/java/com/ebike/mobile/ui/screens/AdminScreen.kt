package com.ebike.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ebike.mobile.data.models.Booking
import com.ebike.mobile.ui.viewmodels.BookingViewModel
import timber.log.Timber

@Composable
fun AdminPanelScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val bookingViewModel = remember { BookingViewModel(context) }
    
    val adminBookings by bookingViewModel.adminBookings.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val errorMessage by bookingViewModel.errorMessage.collectAsState()
    
    var selectedTab by remember { mutableStateOf(AdminTab.BOOKINGS) }
    
    LaunchedEffect(Unit) {
        bookingViewModel.getAdminBookings()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            color = Color(0xFF10B981),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Manage rentals & bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            contentColor = Color(0xFF10B981)
        ) {
            AdminTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title, fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                )
            }
        }
        
        // Content
        when (selectedTab) {
            AdminTab.BOOKINGS -> AdminBookingsTab(bookingViewModel, isLoading, errorMessage, adminBookings)
            AdminTab.STATISTICS -> AdminStatisticsTab(adminBookings)
            AdminTab.SETTINGS -> AdminSettingsTab(navController)
        }
    }
}

@Composable
fun AdminBookingsTab(
    bookingViewModel: BookingViewModel,
    isLoading: Boolean,
    errorMessage: String?,
    bookings: List<Booking>
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Red
                    )
                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
        bookings.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = "No bookings",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "No bookings to manage",
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Gray
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings) { booking ->
                    AdminBookingCard(booking, bookingViewModel)
                }
            }
        }
    }
}

@Composable
fun AdminBookingCard(booking: Booking, viewModel: BookingViewModel) {
    var showActionMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Booking #${booking.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = booking.bike?.name ?: "Unknown Bike",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Box {
                    IconButton(onClick = { showActionMenu = !showActionMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }
                    DropdownMenu(
                        expanded = showActionMenu,
                        onDismissRequest = { showActionMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Confirm") },
                            onClick = {
                                showActionMenu = false
                                // TODO: Implement confirm action
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Complete") },
                            onClick = {
                                showActionMenu = false
                                // TODO: Implement complete action
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancel") },
                            onClick = {
                                showActionMenu = false
                                // TODO: Implement cancel action
                            }
                        )
                    }
                }
            }
            
            Divider()
            
            // Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Start", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(booking.startTime, style = MaterialTheme.typography.bodySmall)
                }
                Column {
                    Text("End", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(booking.endTime ?: "Ongoing", style = MaterialTheme.typography.bodySmall)
                }
                Column {
                    Text("Cost", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        "₹${booking.totalCost ?: "0"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
            
            // Status Badge
            Surface(
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(6.dp),
                color = getStatusColor(booking.status)
            ) {
                Text(
                    text = booking.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AdminStatisticsTab(bookings: List<Booking>) {
    val totalBookings = bookings.size
    val completedBookings = bookings.count { it.status == "COMPLETED" }
    val activeBookings = bookings.count { it.status in listOf("ACTIVE", "PENDING", "APPROVED") }
    val cancelledBookings = bookings.count { it.status == "CANCELLED" }
    val totalRevenue = bookings
        .filter { it.status == "COMPLETED" }
        .sumOf { (it.totalCost ?: 0.0).toDouble() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Bookings",
                value = totalBookings.toString(),
                icon = Icons.Default.BookmarkAdd,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Completed",
                value = completedBookings.toString(),
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Active",
                value = activeBookings.toString(),
                icon = Icons.Default.DirectionsBike,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Cancelled",
                value = cancelledBookings.toString(),
                icon = Icons.Default.Cancel,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Revenue Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Revenue",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = "₹${String.format("%.2f", totalRevenue)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF10B981),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminSettingsTab(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Settings Items
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Manage booking notifications",
            onClick = { }
        )
        
        SettingsItem(
            icon = Icons.Default.Security,
            title = "Security",
            description = "Update password and security settings",
            onClick = { }
        )
        
        SettingsItem(
            icon = Icons.Default.Info,
            title = "About",
            description = "App version and information",
            onClick = { }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button
        Button(
            onClick = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.AdminPanel.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", color = Color.White)
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF10B981)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF10B981)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF10B981),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class AdminTab(val title: String) {
    BOOKINGS("Bookings"),
    STATISTICS("Statistics"),
    SETTINGS("Settings")
}
