package com.ebike.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebike.mobile.api.RetrofitClient
import com.ebike.mobile.data.models.Bike
import com.ebike.mobile.ui.viewmodels.BikeViewModel

@Composable
fun BikeListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = remember { BikeViewModel(context) }
    
    val bikes by viewModel.bikes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("ALL") }

    val filteredBikes by remember(bikes, searchQuery, selectedStatus) {
        derivedStateOf {
            bikes.filter { bike ->
                val matchesStatus = selectedStatus == "ALL" || bike.status.equals(selectedStatus, ignoreCase = true)
                val matchesQuery = searchQuery.isBlank() || bike.name
                    ?.startsWith(searchQuery.trim(), ignoreCase = true) == true

                matchesStatus && matchesQuery
            }
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.getAllBikes()
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Available Bikes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
        
        // Search and Filters
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search bikes"
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    } else null,
                    placeholder = { Text("Search bike name") },
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusFilterButton(
                        label = "All",
                        selected = selectedStatus == "ALL",
                        onClick = { selectedStatus = "ALL" },
                        modifier = Modifier.weight(1f)
                    )
                    StatusFilterButton(
                        label = "Available",
                        selected = selectedStatus == "AVAILABLE",
                        onClick = { selectedStatus = "AVAILABLE" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusFilterButton(
                        label = "Rented",
                        selected = selectedStatus == "RENTED",
                        onClick = { selectedStatus = "RENTED" },
                        modifier = Modifier.weight(1f)
                    )
                    StatusFilterButton(
                        label = "Maintenance",
                        selected = selectedStatus == "MAINTENANCE",
                        onClick = { selectedStatus = "MAINTENANCE" },
                        modifier = Modifier.weight(1f)
                    )
                }
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
                    modifier = Modifier.fillMaxSize(),
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
            filteredBikes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (bikes.isEmpty()) "No bikes available" else "No bikes match your search/filter",
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBikes) { bike ->
                        BikeCard(bike) {
                            navController.navigate(Screen.BikeDetail.createRoute(bike.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BikeCard(bike: Bike, onClick: () -> Unit) {
    val context = LocalContext.current
    val bikeImage = resolveBikeImageModel(context, bike)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = Color(0xFFE8F7F1)
            ) {
                if (bikeImage != null) {
                    AsyncImage(
                        model = bikeImage,
                        contentDescription = bike.name ?: "Bike",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "Bike",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bike.name ?: bike.bikeCode ?: "Bike #${bike.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = bike.model ?: "Standard",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (bike.status) {
                            "AVAILABLE" -> Color(0xFFD1F7E5)
                            "RENTED" -> Color(0xFFFFE5E5)
                            else -> Color(0xFFF0F0F0)
                        }
                    ) {
                        Text(
                            text = bike.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (bike.status) {
                                "AVAILABLE" -> Color(0xFF059669)
                                "RENTED" -> Color(0xFFDC2626)
                                else -> Color.Gray
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "₹${String.format("%.2f", if (bike.pricePerHour > 0) bike.pricePerHour else bike.hourlyRate)}/hr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color(0xFF10B981)
            )
        }
    }
}

private fun resolveBikeImageModel(context: android.content.Context, bike: Bike): Any? {
    val raw = bike.imageUrl ?: bike.image
    if (raw.isNullOrBlank()) return null
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    if (raw.startsWith("data:")) return decodeBase64Image(raw)
    decodeBase64Image(raw)?.let { return it }

    val baseUrl = RetrofitClient.getBaseUrl(context)
    val serverBase = if (baseUrl.endsWith("/api/")) {
        baseUrl.removeSuffix("api/")
    } else if (baseUrl.endsWith("/api")) {
        baseUrl.removeSuffix("api")
    } else {
        baseUrl
    }.trimEnd('/')

    return when {
        raw.startsWith("/api/") -> serverBase + raw
        raw.startsWith("api/") -> serverBase + "/" + raw
        raw.startsWith("/uploads/") -> serverBase + raw
        raw.startsWith("uploads/") -> serverBase + "/" + raw
        raw.startsWith("/") -> serverBase + raw
        else -> serverBase + "/" + raw
    }
}

private fun decodeBase64Image(raw: String): android.graphics.Bitmap? {
    return try {
        val cleaned = raw
            .substringAfter("base64,", raw)
            .replace("\n", "")
            .replace("\r", "")
            .trim()

        if (!looksLikeBase64Image(cleaned)) return null

        val bytes = Base64.decode(cleaned, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

private fun looksLikeBase64Image(value: String): Boolean {
    if (value.startsWith("/9j/") || value.startsWith("iVBOR") || value.startsWith("R0lGOD")) {
        return true
    }
    return value.length > 100 && value.matches(Regex("^[A-Za-z0-9+/=\\s]+$"))
}

@Composable
private fun StatusFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF10B981) else Color(0xFFF3F4F6),
            contentColor = if (selected) Color.White else Color(0xFF374151)
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
