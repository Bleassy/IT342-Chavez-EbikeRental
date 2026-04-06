package com.ebike.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebike.mobile.data.models.Booking
import com.ebike.mobile.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BookingViewModel(private val context: Context) : ViewModel() {
    
    private val repository = BookingRepository(context)
    
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings
    
    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _createBookingResult = MutableStateFlow<Result<Booking>?>(null)
    val createBookingResult: StateFlow<Result<Booking>?> = _createBookingResult
    
    private val _cancelBookingResult = MutableStateFlow<Result<Booking>?>(null)
    val cancelBookingResult: StateFlow<Result<Booking>?> = _cancelBookingResult
    
    fun createBooking(bikeId: Long, startTime: String, endTime: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.createBooking(bikeId, startTime, endTime)
            _createBookingResult.value = result
            
            result.onSuccess { booking ->
                _selectedBooking.value = booking
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to create booking"
                Timber.e(error, "Create booking error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun getUserBookings(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getUserBookings(page)
            
            result.onSuccess { bookingList ->
                _bookings.value = bookingList
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch bookings"
                Timber.e(error, "Get user bookings error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun getBookingDetail(bookingId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getBookingDetail(bookingId)
            
            result.onSuccess { booking ->
                _selectedBooking.value = booking
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch booking details"
                Timber.e(error, "Get booking detail error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun cancelBooking(bookingId: Long, reason: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.cancelBooking(bookingId, reason)
            _cancelBookingResult.value = result
            
            result.onSuccess { booking ->
                _selectedBooking.value = booking
                // Update in list
                _bookings.value = _bookings.value.map {
                    if (it.id == booking.id) booking else it
                }
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to cancel booking"
                Timber.e(error, "Cancel booking error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun completeBooking(bookingId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.completeBooking(bookingId)
            
            result.onSuccess { booking ->
                _selectedBooking.value = booking
                // Update in list
                _bookings.value = _bookings.value.map {
                    if (it.id == booking.id) booking else it
                }
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to complete booking"
                Timber.e(error, "Complete booking error")
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSelectedBooking() {
        _selectedBooking.value = null
    }
}
