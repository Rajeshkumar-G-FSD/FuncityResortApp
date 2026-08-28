package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BlockedRoom
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.CalendarDay
import com.example.data.model.ResortMetrics
import com.example.data.model.RoomCategory
import com.example.data.model.RoomDataDefaults
import com.example.data.model.RoomInfo
import com.example.data.model.RoomSlotStatus
import com.example.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppNavTab(val title: String) {
    DASHBOARD("Dashboard"),
    ROOM_CALENDAR("Room calendar"),
    BOOKINGS("Bookings"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings")
}

data class SlotDetailModalData(
    val room: RoomInfo,
    val dateStr: String,
    val isoDate: String,
    val status: RoomSlotStatus,
    val booking: Booking? = null,
    val blockedRoom: BlockedRoom? = null
)

class ResortViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookingRepository.getInstance(application)

    val bookings: StateFlow<List<Booking>> = repository.bookings
    val blockedRooms: StateFlow<List<BlockedRoom>> = repository.blockedRooms
    val metrics: StateFlow<ResortMetrics> = repository.metrics
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val syncMessage: StateFlow<String?> = repository.syncMessage

    // Navigation & Auth State
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Room Calendar Date Range & Navigation
    private val _calendarStartOffsetDays = MutableStateFlow(0)
    val calendarStartOffsetDays: StateFlow<Int> = _calendarStartOffsetDays.asStateFlow()

    private val _selectedRoomCategoryFilter = MutableStateFlow<String?>("All")
    val selectedRoomCategoryFilter: StateFlow<String?> = _selectedRoomCategoryFilter.asStateFlow()

    // Calendar slot detail modal (for inspecting or toggling)
    private val _activeSlotDetail = MutableStateFlow<SlotDetailModalData?>(null)
    val activeSlotDetail: StateFlow<SlotDetailModalData?> = _activeSlotDetail.asStateFlow()

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

    // Room Calendar Operations
    fun nextCalendarDays(days: Int = 14) {
        _calendarStartOffsetDays.value += days
    }

    fun prevCalendarDays(days: Int = 14) {
        _calendarStartOffsetDays.value -= days
    }

    fun jumpCalendarToToday() {
        _calendarStartOffsetDays.value = 0
    }

    fun setRoomCategoryFilter(category: String?) {
        _selectedRoomCategoryFilter.value = category
    }

    fun toggleRoomBlock(roomId: String, isoDate: String, roomType: String) {
        val cleanRoomId = roomId.replace("Room ", "").trim()
        val isAlreadyBlocked = blockedRooms.value.any { b ->
            val rId = b.roomId.replace("Room ", "").trim()
            rId == cleanRoomId && b.date == isoDate
        }

        viewModelScope.launch {
            if (isAlreadyBlocked) {
                repository.unblockRoom(cleanRoomId, isoDate)
                _toastMessage.value = "Room $cleanRoomId unblocked for $isoDate"
            } else {
                repository.blockRoom(cleanRoomId, isoDate, roomType)
                _toastMessage.value = "Room $cleanRoomId blocked on $isoDate"
            }
        }
    }

    fun blockRoom(roomId: String, isoDate: String, roomType: String, reason: String = "Blocked by admin") {
        viewModelScope.launch {
            repository.blockRoom(roomId, isoDate, roomType, reason)
            _toastMessage.value = "Room $roomId blocked on $isoDate"
        }
    }

    fun unblockRoom(roomId: String, isoDate: String) {
        viewModelScope.launch {
            repository.unblockRoom(roomId, isoDate)
            _toastMessage.value = "Room $roomId unblocked for $isoDate"
        }
    }

    fun blockRoomForVisibleRange(roomId: String, dates: List<String>, roomType: String) {
        viewModelScope.launch {
            repository.batchBlockRoom(roomId, dates, roomType)
            _toastMessage.value = "Blocked Room $roomId for ${dates.size} days"
        }
    }

    fun unblockRoomForVisibleRange(roomId: String, dates: List<String>) {
        viewModelScope.launch {
            repository.batchUnblockRoom(roomId, dates)
            _toastMessage.value = "Unblocked Room $roomId for ${dates.size} days"
        }
    }

    fun openSlotDetail(slotDetail: SlotDetailModalData) {
        _activeSlotDetail.value = slotDetail
    }

    fun closeSlotDetail() {
        _activeSlotDetail.value = null
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
            _toastMessage.value = "Sample data & blocks seeded to Firebase"
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

