package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.ResortMetrics
import com.example.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    DASHBOARD("Dashboard"),
    BOOKINGS("Bookings"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings")
}

class ResortViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookingRepository.getInstance(application)

    val bookings: StateFlow<List<Booking>> = repository.bookings
    val metrics: StateFlow<ResortMetrics> = repository.metrics
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val syncMessage: StateFlow<String?> = repository.syncMessage

    // Navigation & Auth State
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Search & Filter in Bookings Tab
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<BookingStatus?>(null)
    val selectedStatusFilter: StateFlow<BookingStatus?> = _selectedStatusFilter.asStateFlow()

    val filteredBookings: StateFlow<List<Booking>> = combine(
        bookings,
        searchQuery,
        selectedStatusFilter
    ) { allBookings, query, statusFilter ->
        allBookings.filter { booking ->
            val matchesQuery = query.isBlank() ||
                booking.guestName.contains(query, ignoreCase = true) ||
                booking.id.contains(query, ignoreCase = true) ||
                booking.roomNumber.contains(query, ignoreCase = true) ||
                booking.contactNumber.contains(query, ignoreCase = true)

            val matchesStatus = statusFilter == null || booking.status == statusFilter

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog & Edit State
    private val _editingBooking = MutableStateFlow<Booking?>(null)
    val editingBooking: StateFlow<Booking?> = _editingBooking.asStateFlow()

    private val _isBookingFormOpen = MutableStateFlow(false)
    val isBookingFormOpen: StateFlow<Boolean> = _isBookingFormOpen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Currency Preference
    private val _currencySymbol = MutableStateFlow("₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    fun login(email: String, pass: String): Boolean {
        if (email.isNotBlank() && pass.isNotBlank()) {
            _isLoggedIn.value = true
            _toastMessage.value = "Welcome to Funcity Resorts Dashboard"
            return true
        }
        _toastMessage.value = "Please enter valid credentials"
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        _toastMessage.value = "Signed out"
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: BookingStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, newStatus)
            _toastMessage.value = "Updated $bookingId to ${newStatus.label}"
        }
    }

    fun openNewBookingForm() {
        _editingBooking.value = null
        _isBookingFormOpen.value = true
    }

    fun openEditBookingForm(booking: Booking) {
        _editingBooking.value = booking
        _isBookingFormOpen.value = true
    }

    fun closeBookingForm() {
        _isBookingFormOpen.value = false
        _editingBooking.value = null
    }

    fun saveBooking(booking: Booking) {
        viewModelScope.launch {
            if (_editingBooking.value != null) {
                repository.updateBooking(booking)
                _toastMessage.value = "Booking updated in Firebase"
            } else {
                repository.createBooking(booking)
                _toastMessage.value = "New booking created in Firebase"
            }
            closeBookingForm()
        }
    }

    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            repository.deleteBooking(bookingId)
            _toastMessage.value = "Booking $bookingId deleted"
        }
    }

    fun seedData() {
        viewModelScope.launch {
            repository.seedSampleDataToFirestore()
            _toastMessage.value = "Sample data seeded to Firebase"
        }
    }

    fun refresh() {
        repository.refresh()
        _toastMessage.value = "Refreshing data from Firebase..."
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
