package com.ebike.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebike.mobile.data.models.Bike
import com.ebike.mobile.data.repository.BikeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BikeViewModel(private val context: Context) : ViewModel() {
    
    private val repository = BikeRepository(context)
    
    private val _bikes = MutableStateFlow<List<Bike>>(emptyList())
    val bikes: StateFlow<List<Bike>> = _bikes
    
    private val _selectedBike = MutableStateFlow<Bike?>(null)
    val selectedBike: StateFlow<Bike?> = _selectedBike
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _nearbyBikes = MutableStateFlow<List<Bike>>(emptyList())
    val nearbyBikes: StateFlow<List<Bike>> = _nearbyBikes
    
    fun getAllBikes(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getAllBikes(page)
            
            result.onSuccess { bikeList ->
                _bikes.value = bikeList
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch bikes"
                Timber.e(error, "Get all bikes error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun getBikeDetail(bikeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getBikeDetail(bikeId)
            
            result.onSuccess { bike ->
                _selectedBike.value = bike
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch bike details"
                Timber.e(error, "Get bike detail error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun searchBikes(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.searchBikes(query)
            
            result.onSuccess { bikeList ->
                _bikes.value = bikeList
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Search failed"
                Timber.e(error, "Search bikes error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun getNearbyBikes(latitude: Double, longitude: Double, radius: Double = 5.0) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getNearbyBikes(latitude, longitude, radius)
            
            result.onSuccess { bikeList ->
                _nearbyBikes.value = bikeList
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch nearby bikes"
                Timber.e(error, "Get nearby bikes error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSelectedBike() {
        _selectedBike.value = null
    }
}
