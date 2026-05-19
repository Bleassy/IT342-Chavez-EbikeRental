package com.ebike.mobile.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebike.mobile.api.RetrofitClient
import com.ebike.mobile.data.models.Booking
import com.ebike.mobile.ui.viewmodels.AuthViewModel
import com.ebike.mobile.ui.viewmodels.BikeViewModel
import com.ebike.mobile.ui.viewmodels.BookingViewModel
import com.ebike.mobile.utils.ImageUtils
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun BikeDetailScreen(navController: NavHostController, bikeId: Long) {
    val context = LocalContext.current
    val bikeViewModel = remember { BikeViewModel(context) }
    val bookingViewModel = remember { BookingViewModel(context) }
    
    val selectedBike by bikeViewModel.selectedBike.collectAsState()
    val isLoading by bikeViewModel.isLoading.collectAsState()
    val errorMessage by bikeViewModel.errorMessage.collectAsState()
    val createBookingResult by bookingViewModel.createBookingResult.collectAsState()
    
    val scope = rememberCoroutineScope()
    var showDateTimePicker by remember { mutableStateOf(false) }
    var selectedStartTime by remember { mutableStateOf("") }
    var selectedEndTime by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf(1) }
    var bookingError by remember { mutableStateOf("") }
    
    LaunchedEffect(bikeId) {
        bikeViewModel.getBikeDetail(bikeId)
    }
    
    LaunchedEffect(createBookingResult) {
        if (createBookingResult?.isSuccess == true) {
            val booking = createBookingResult?.getOrNull()
            if (booking != null) {
                navController.navigate(Screen.BookingConfirmation.createRoute(booking.id)) {
                    popUpTo(Screen.BikeDetail.route) { inclusive = true }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            color = Color(0xFF10B981),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Bike Details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        } else if (selectedBike != null) {
            val bike = selectedBike!!
            val bikeImage = resolveBikeImageUrl(context, bike.imageUrl ?: bike.image)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bike Image Placeholder
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE0E0E0)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!bikeImage.isNullOrBlank()) {
                            AsyncImage(
                                model = bikeImage,
                                contentDescription = bike.name ?: "Bike",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = "Bike",
                                modifier = Modifier.size(100.dp),
                                tint = Color(0xFF10B981)
                            )
                        }
                    }
                }
                
                // Bike Info
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
                        // Display bike name/model
                        val displayName = bike.name ?: bike.bikeCode ?: bike.model ?: "Bike #${bike.id}"
                        Text(
                            text = displayName ?: "Unknown Bike",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                        
                        // Display model and brand
                        val modelText = when {
                            !bike.model.isNullOrEmpty() && !bike.brand.isNullOrEmpty() -> "${bike.brand} ${bike.model}"
                            !bike.model.isNullOrEmpty() -> "Model: ${bike.model}"
                            !bike.brand.isNullOrEmpty() -> "Brand: ${bike.brand}"
                            else -> "Standard Bike"
                        }
                        Text(
                            text = modelText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        // Display color
                        val colorText = bike.color?.let { "Color: $it" } ?: "Color: Unknown"
                        Text(
                            text = colorText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        // Display bike code and year if available
                        if (!bike.bikeCode.isNullOrEmpty() || bike.year != null) {
                            val bikeCodeText = buildString {
                                bike.bikeCode?.let { append("Code: $it") }
                                if (bike.year != null && !bike.bikeCode.isNullOrEmpty()) append(" | ")
                                bike.year?.let { append("Year: $it") }
                            }
                            if (bikeCodeText.isNotEmpty()) {
                                Text(
                                    text = bikeCodeText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Battery",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${bike.batteryLevel}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF10B981)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "Hourly Rate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                val hourlyPrice = if (bike.pricePerHour > 0) bike.pricePerHour else bike.hourlyRate
                                Text(
                                    text = "₹${String.format("%.2f", hourlyPrice)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF10B981)
                                )
                            }
                            
                            if (!bike.location.isNullOrEmpty()) {
                                Column {
                                    Text(
                                        text = "Location",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = bike.location ?: "N/A",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF10B981),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        
                        // Display description if available
                        if (!bike.description.isNullOrEmpty()) {
                            Text(
                                text = bike.description ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        // Display condition and type if available
                        val typeCondition = buildString {
                            bike.type?.let { append("Type: $it") }
                            if (!bike.type.isNullOrEmpty() && !bike.condition.isNullOrEmpty()) append(" | ")
                            bike.condition?.let { append("Condition: $it") }
                        }
                        if (typeCondition.isNotEmpty()) {
                            Text(
                                text = typeCondition,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                // Book Button
                val isAvailable = bike.status?.uppercase() == "AVAILABLE"
                if (isAvailable) {
                    Button(
                        onClick = { showDateTimePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Book",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Book Now", color = Color.White)
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0E0E0)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Status: ${bike.status ?: "Unknown"}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Red
                    )
                    Text(errorMessage!!, textAlign = TextAlign.Center)
                }
            }
        }
    }
    
    // Booking Date/Time Picker Dialog
    if (showDateTimePicker && selectedBike != null) {
        val bikeForBooking = selectedBike!!
        val bikeDisplayName = bikeForBooking.name
            ?: bikeForBooking.bikeCode
            ?: bikeForBooking.model
            ?: "Bike #${bikeForBooking.id}"
        val hourlyRate = when {
            bikeForBooking.pricePerHour > 0 -> bikeForBooking.pricePerHour
            bikeForBooking.hourlyRate > 0 -> bikeForBooking.hourlyRate
            else -> 20.0
        }
        val estimatedTotal = hours * hourlyRate

        AlertDialog(
            onDismissRequest = { showDateTimePicker = false },
            title = { Text("Book $bikeDisplayName") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (bookingError.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = bookingError,
                                color = Color(0xFFC62828),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    Text(
                        text = "Hourly Rate: ₹${String.format("%.2f", hourlyRate)}/hour",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    Text(
                        text = "Duration (hours)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { if (hours > 1) hours-- },
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease duration",
                                tint = Color.White
                            )
                        }
                        
                        Text(
                            text = hours.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Button(
                            onClick = { if (hours < 24) hours++ },
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase duration",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = "Estimated Total: ₹${String.format("%.2f", estimatedTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF10B981),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hours > 0) {
                            scope.launch {
                                // Generate current timestamp for start time
                                val now = java.time.LocalDateTime.now()
                                val startTime = now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                // Add hours for end time
                                val endTime = now.plusHours(hours.toLong()).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                
                                bookingViewModel.createBooking(
                                    bikeId = bikeForBooking.id,
                                    startTime = startTime,
                                    endTime = endTime
                                )
                                showDateTimePicker = false
                                hours = 1
                            }
                        } else {
                            bookingError = "Please select valid duration"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("Confirm Booking", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookingConfirmationScreen(navController: NavHostController, bookingId: Long) {
    val context = LocalContext.current
    val viewModel = remember { BookingViewModel(context) }
    
    val selectedBooking by viewModel.selectedBooking.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    LaunchedEffect(bookingId) {
        viewModel.getBookingDetail(bookingId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Success Icon
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
                Text(
                    text = "Booking Confirmed!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        } else if (selectedBooking != null) {
            val booking = selectedBooking!!
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Booking Details Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Booking ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "#${booking.id}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF10B981),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Bike: ${booking.bike?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Start: ${formatDateTime(booking.startTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "End: ${if (!booking.endTime.isNullOrEmpty()) formatDateTime(booking.endTime!!) else "Not specified"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Cost",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "₹${calculateCost(booking)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF10B981),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "Status: ${booking.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10B981)
                        )
                    }
                }
                
                // Info Message
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F9FF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Your booking is confirmed. You can view it anytime in your booking history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action Buttons
                Button(
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.BookingConfirmation.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue Shopping", color = Color.White)
                }
                
                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.BookingHistory.route) {
                            popUpTo(Screen.BookingConfirmation.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Booking History", color = Color(0xFF10B981))
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Red
                    )
                    Text(errorMessage!!, textAlign = TextAlign.Center)
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingHistoryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = remember { BookingViewModel(context) }
    
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cancelResult by viewModel.cancelBookingResult.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getRentalHistory()
    }
    
    LaunchedEffect(cancelResult) {
        cancelResult?.onSuccess {
            viewModel.getRentalHistory()
        }
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
                .height(120.dp),
            color = Color(0xFF10B981),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Booking History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Content
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
                            text = errorMessage!!,
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = "No bookings",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "No bookings yet",
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center,
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
                    items(bookings.size) { index ->
                        val booking = bookings[index]
                        BookingCard(booking, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, viewModel: BookingViewModel) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = booking.bike?.name ?: "Unknown Bike",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = "ID: #${booking.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
                Surface(
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
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formatDateTime(booking.startTime),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column {
                    Text(
                        text = "End Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = booking.endTime?.let { formatDateTime(it) } ?: "Ongoing",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Cost",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "₹${calculateCost(booking)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF10B981),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
            
            if (booking.cancellationReason != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Cancellation Reason",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            text = booking.cancellationReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
            
            // Cancel Button (for active bookings)
            if (booking.status in listOf("PENDING", "CONFIRMED", "APPROVED", "ACTIVE")) {
                Button(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Cancel",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Booking", color = Color.White, fontSize = MaterialTheme.typography.labelMedium.fontSize, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
    
    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking") },
            text = {
                Column {
                    Text("Please provide a reason for cancellation:")
                    TextField(
                        value = cancellationReason,
                        onValueChange = { cancellationReason = it },
                        placeholder = { Text("Enter reason...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancellationReason.isNotBlank()) {
                            viewModel.cancelBooking(booking.id, cancellationReason)
                            showCancelDialog = false
                        }
                    }
                ) {
                    Text("Cancel Booking")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "PENDING" -> Color(0xFFFFA500)
        "APPROVED" -> Color(0xFF4CAF50)
        "ACTIVE" -> Color(0xFF2196F3)
        "COMPLETED" -> Color(0xFF10B981)
        "CANCELLED" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val editMode = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var uploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Timber.d("Image selected: $uri")
            
            // Auto-upload the image
            scope.launch {
                uploadingImage = true
                uploadError = null
                try {
                    val base64 = ImageUtils.compressImageToBase64(context, uri)
                    if (base64 != null) {
                        if (ImageUtils.isImageSizeAcceptable(base64)) {
                            authViewModel.uploadProfilePicture(base64)
                            Timber.d("Image uploaded successfully")
                        } else {
                            uploadError = "Image is too large (max 5MB)"
                            Timber.e("Image size exceeded 5MB")
                            selectedImageUri = null
                        }
                    } else {
                        uploadError = "Failed to compress image"
                        Timber.e("Image compression failed")
                        selectedImageUri = null
                    }
                } catch (e: Exception) {
                    uploadError = "Error: ${e.message}"
                    Timber.e(e, "Image upload error")
                    selectedImageUri = null
                } finally {
                    uploadingImage = false
                }
            }
        }
    }
    
    // Update form fields when user data changes (real-time)
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            fullName = user.fullName.ifBlank { fullName }
            email = user.email.ifBlank { email }
            phone = user.phone?.ifBlank { phone } ?: phone
            address = user.address?.ifBlank { address } ?: address
            Timber.d("✅ Profile synced: ${user.email} (${user.fullName})")
            Timber.d("   Phone: $phone, Address: $address")
        } ?: run {
            Timber.w("⚠️ currentUser is NULL - not logged in")
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            color = Color(0xFF10B981),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "My Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                
                IconButton(
                    onClick = { editMode.value = !editMode.value }
                ) {
                    Icon(
                        imageVector = if (editMode.value) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = if (editMode.value) "Close" else "Edit",
                        tint = Color.White
                    )
                }
            }
        }
        
        // Profile Content
        if (currentUser == null) {
            // Not logged in
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Not logged in",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF10B981)
                    )
                    Text(
                        text = "Please log in to view your profile",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Logged in - show profile
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Avatar with Upload Button
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        // Show selected image immediately (before upload completes)
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(50)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!currentUser?.profilePic.isNullOrEmpty()) {
                        // Show uploaded profile picture from backend
                        AsyncImage(
                            model = currentUser?.profilePic,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(50)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Show default avatar
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            tint = Color(0xFF10B981)
                        )
                    }
                    
                    // Upload indicator
                    if (uploadingImage) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                    
                    // Camera icon for upload hint
                    if (!uploadingImage) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp),
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF10B981),
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Upload Photo",
                                modifier = Modifier.padding(8.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Upload status messages
                if (uploadError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = uploadError ?: "",
                                color = Color(0xFFC62828),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                if (uploadingImage) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F7F1)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF10B981),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Uploading profile picture... (may take 30-120 seconds for large images)",
                                color = Color(0xFF10B981),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                if (errorMessage != null && errorMessage != uploadError) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                Text(
                    text = if (uploadingImage) "Upload in progress..." else "Tap photo to upload profile picture",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uploadingImage) Color(0xFF10B981) else Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                if (!editMode.value) {
                    // View Mode
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileField("Full Name", fullName.ifBlank { "Not provided" })
                            Divider()
                            ProfileField("Email", email.ifBlank { "Not provided" })
                            Divider()
                            ProfileField("Phone", phone.ifBlank { "Not provided" })
                            Divider()
                            ProfileField("Address", address.ifBlank { "Not provided" })
                        }
                    }
                } else {
                    // Edit Mode
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            TextField(
                                value = email,
                                onValueChange = { },
                                label = { Text("Email") },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            TextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            TextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                            
                            Button(
                                onClick = {
                                    authViewModel.updateProfile(
                                        fullName = fullName,
                                        phone = phone,
                                        address = address,
                                        onComplete = { success ->
                                            if (success) {
                                                editMode.value = false
                                                authViewModel.refreshUserProfile()
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Save Changes", color = Color.White)
                                }
                            }
                        }
                    }
                }
                
                // Logout Button
                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

/**
 * Format date-time string for display
 * Handles both ISO_DATE_TIME format and already formatted strings
 */
fun formatDateTime(dateTimeString: String?): String {
    if (dateTimeString.isNullOrEmpty()) return "N/A"
    return try {
        // Try parsing ISO format (2026-05-17T10:30:00)
        val dateTime = java.time.LocalDateTime.parse(dateTimeString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a")
        dateTime.format(formatter)
    } catch (e: Exception) {
        // If parsing fails, return original string
        dateTimeString
    }
}

/**
 * Calculate total cost based on booking duration
 * Returns the totalCost if available, otherwise calculates from start/end time
 * Assumes ₹20 per hour if duration can be calculated
 */
fun calculateCost(booking: com.ebike.mobile.data.models.Booking): String {
    // If totalCost is available and not zero, use it
    if (booking.totalCost != null && booking.totalCost!! > 0) {
        return String.format("%.2f", booking.totalCost)
    }
    
    // Calculate cost from duration (₹20 per hour)
    return try {
        if (booking.startTime.isNullOrEmpty() || booking.endTime.isNullOrEmpty()) {
            "0.00" // Ongoing booking
        } else {
            val startTime = java.time.LocalDateTime.parse(booking.startTime ?: "")
            val endTime = java.time.LocalDateTime.parse(booking.endTime ?: "")
            val duration = java.time.Duration.between(startTime, endTime)
            val hours = kotlin.math.max(1.0, duration.toMinutes() / 60.0) // Minimum 1 hour
            val cost = hours * 20.0 // ₹20 per hour
            String.format("%.2f", cost)
        }
    } catch (e: Exception) {
        Timber.e(e, "Error calculating cost")
        "0.00" // Default if parsing fails
    }
}

private fun resolveBikeImageUrl(context: android.content.Context, rawImage: String?): String? {
    if (rawImage.isNullOrBlank()) return null
    if (rawImage.startsWith("http://") || rawImage.startsWith("https://") || rawImage.startsWith("data:")) return rawImage
    if (looksLikeBase64Image(rawImage)) return "data:image/jpeg;base64,$rawImage"

    val baseUrl = RetrofitClient.getBaseUrl(context)
    val serverBase = if (baseUrl.endsWith("/api/")) {
        baseUrl.removeSuffix("api/")
    } else if (baseUrl.endsWith("/api")) {
        baseUrl.removeSuffix("api")
    } else {
        baseUrl
    }

    val normalizedPath = if (rawImage.startsWith('/')) rawImage.substring(1) else rawImage
    return serverBase.trimEnd('/') + "/" + normalizedPath
}

private fun looksLikeBase64Image(value: String): Boolean {
    if (value.startsWith("/9j/") || value.startsWith("iVBOR") || value.startsWith("R0lGOD")) {
        return true
    }
    return value.length > 100 && value.matches(Regex("^[A-Za-z0-9+/=\\s]+$"))
}
