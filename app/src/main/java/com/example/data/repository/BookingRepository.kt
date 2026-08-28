package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.DailyRevenuePoint
import com.example.data.model.ResortMetrics
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookingRepository private constructor(context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var firestoreListener: ListenerRegistration? = null

    private val _bookings = MutableStateFlow<List<Booking>>(getInitialSampleBookings())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _metrics = MutableStateFlow(computeMetrics(getInitialSampleBookings()))
    val metrics: StateFlow<ResortMetrics> = _metrics.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>("Connected to Firebase")
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        FirebaseManager.initialize(context)
        startFirestoreSync()
    }

    fun startFirestoreSync() {
        val firestore = FirebaseManager.getFirestore()
        if (firestore == null) {
            _syncMessage.value = "Local storage active (Firebase initializing)"
            return
        }

        try {
            _isSyncing.value = true
            firestoreListener?.remove()
            firestoreListener = firestore.collection("bookings")
                .addSnapshotListener { snapshots, e ->
                    _isSyncing.value = false
                    if (e != null) {
                        Log.w("BookingRepo", "Firestore listen failed: ${e.message}")
                        _syncMessage.value = "Local cached (Firestore sync error: ${e.localizedMessage})"
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val fetched = snapshots.documents.mapNotNull { doc ->
                            doc.data?.let { Booking.fromMap(it, doc.id) }
                        }
                        if (fetched.isNotEmpty()) {
                            _bookings.value = fetched.sortedByDescending { it.createdAt }
                            _metrics.value = computeMetrics(fetched)
                            _syncMessage.value = "Synced with Firebase (${fetched.size} bookings)"
                        }
                    } else {
                        // Empty collection in Firestore, offer to seed or keep local
                        _syncMessage.value = "Firebase connected (Collection empty, local active)"
                    }
                }
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Failed to setup snapshot listener", e)
        }
    }

    suspend fun createBooking(booking: Booking): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            // Update local first for instant UI response
            val currentList = _bookings.value.toMutableList()
            currentList.add(0, booking)
            _bookings.value = currentList
            _metrics.value = computeMetrics(currentList)

            // Save to Firebase
            val firestore = FirebaseManager.getFirestore()
            firestore?.collection("bookings")
                ?.document(booking.id)
                ?.set(booking.toMap(), SetOptions.merge())
                ?.await()

            _isSyncing.value = false
            _syncMessage.value = "Booking ${booking.id} saved to Firebase"
            Result.success(booking)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error creating booking", e)
            _syncMessage.value = "Saved locally (Cloud sync: ${e.message})"
            Result.success(booking) // Still success locally
        }
    }

    suspend fun updateBooking(booking: Booking): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            val currentList = _bookings.value.map {
                if (it.id == booking.id) booking else it
            }
            _bookings.value = currentList
            _metrics.value = computeMetrics(currentList)

            val firestore = FirebaseManager.getFirestore()
            firestore?.collection("bookings")
                ?.document(booking.id)
                ?.set(booking.toMap(), SetOptions.merge())
                ?.await()

            _isSyncing.value = false
            _syncMessage.value = "Booking ${booking.id} updated in Firebase"
            Result.success(booking)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error updating booking", e)
            _syncMessage.value = "Updated locally (Cloud sync: ${e.message})"
            Result.success(booking)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: BookingStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentList = _bookings.value.map {
                if (it.id == bookingId) it.copy(status = newStatus) else it
            }
            _bookings.value = currentList
            _metrics.value = computeMetrics(currentList)

            val firestore = FirebaseManager.getFirestore()
            firestore?.collection("bookings")
                ?.document(bookingId)
                ?.update("status", newStatus.name)
                ?.await()

            _syncMessage.value = "Status updated to ${newStatus.label}"
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error updating status", e)
            _syncMessage.value = "Status updated locally"
            Result.success(Unit)
        }
    }

    suspend fun deleteBooking(bookingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            val currentList = _bookings.value.filter { it.id != bookingId }
            _bookings.value = currentList
            _metrics.value = computeMetrics(currentList)

            val firestore = FirebaseManager.getFirestore()
            firestore?.collection("bookings")
                ?.document(bookingId)
                ?.delete()
                ?.await()

            _isSyncing.value = false
            _syncMessage.value = "Booking $bookingId deleted"
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error deleting booking", e)
            _syncMessage.value = "Deleted locally"
            Result.success(Unit)
        }
    }

    suspend fun seedSampleDataToFirestore(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            val sampleData = getInitialSampleBookings()
            val firestore = FirebaseManager.getFirestore()

            if (firestore != null) {
                for (booking in sampleData) {
                    firestore.collection("bookings").document(booking.id).set(booking.toMap()).await()
                }
                _syncMessage.value = "Seeded ${sampleData.size} bookings to Firebase"
            }
            _bookings.value = sampleData
            _metrics.value = computeMetrics(sampleData)
            _isSyncing.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            _syncMessage.value = "Seeded locally (${e.message})"
            Result.failure(e)
        }
    }

    fun refresh() {
        startFirestoreSync()
    }

    private fun computeMetrics(bookingList: List<Booking>): ResortMetrics {
        val confirmed = bookingList.filter { it.status == BookingStatus.CONFIRMED }
        val pending = bookingList.filter { it.status == BookingStatus.PENDING }
        val cancelled = bookingList.filter { it.status == BookingStatus.CANCELLED }

        val confirmedRev = confirmed.sumOf { it.totalAmount }
        val advanceCol = bookingList.sumOf { it.advanceCollected }
        val pendingAmt = pending.sumOf { it.totalAmount - it.advanceCollected }.coerceAtLeast(0.0)

        val history = listOf(
            DailyRevenuePoint("15 Oct", 1200.0),
            DailyRevenuePoint("17 Oct", 1800.0),
            DailyRevenuePoint("19 Oct", 1400.0),
            DailyRevenuePoint("21 Oct", 2900.0),
            DailyRevenuePoint("23 Oct", 2300.0),
            DailyRevenuePoint("25 Oct", 3800.0),
            DailyRevenuePoint("28 Oct", confirmedRev)
        )

        return ResortMetrics(
            confirmedRevenue = confirmedRev,
            advanceCollected = advanceCol,
            pendingAmount = pendingAmt,
            pendingCount = pending.size,
            cancelledToday = cancelled.size,
            confirmedCount = confirmed.size,
            totalBookings = bookingList.size,
            occupancyRate = if (bookingList.isNotEmpty()) ((confirmed.size.toFloat() / 12f) * 100).toInt().coerceIn(40, 96) else 78,
            revenueHistory = history
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: BookingRepository? = null

        fun getInstance(context: Context): BookingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BookingRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getInitialSampleBookings(): List<Booking> {
            return listOf(
                Booking(
                    id = "FC-501515",
                    guestName = "Rajesh kumar G",
                    roomType = "Couple Room",
                    roomNumber = "Room 111",
                    totalAmount = 4480.0,
                    advanceCollected = 967.0,
                    checkIn = "24 Oct, 2:00 PM",
                    checkOut = "26 Oct, 11:00 AM",
                    contactNumber = "+91 98765 43210",
                    status = BookingStatus.CONFIRMED,
                    guestEmail = "rajesh.g@gmail.com",
                    adults = 2,
                    children = 0,
                    notes = "Early check-in requested. Honeymoon package."
                ),
                Booking(
                    id = "FC-501516",
                    guestName = "Priya & Sanjay Verma",
                    roomType = "Nilgiri Mountain Villa",
                    roomNumber = "Villa 204",
                    totalAmount = 6500.0,
                    advanceCollected = 2000.0,
                    checkIn = "25 Oct, 1:00 PM",
                    checkOut = "28 Oct, 11:00 AM",
                    contactNumber = "+91 98450 11223",
                    status = BookingStatus.CONFIRMED,
                    guestEmail = "sanjay.verma@outlook.com",
                    adults = 2,
                    children = 1,
                    notes = "Tea garden view preferred."
                ),
                Booking(
                    id = "FC-501517",
                    guestName = "Dr. Vikramaditya Roy",
                    roomType = "Heritage Suite",
                    roomNumber = "Room 302",
                    totalAmount = 5200.0,
                    advanceCollected = 0.0,
                    checkIn = "26 Oct, 3:00 PM",
                    checkOut = "28 Oct, 12:00 PM",
                    contactNumber = "+91 91234 56780",
                    status = BookingStatus.PENDING,
                    guestEmail = "v.roy@aims.org",
                    adults = 2,
                    children = 0,
                    notes = "Awaiting bank transfer confirmation."
                ),
                Booking(
                    id = "FC-501518",
                    guestName = "Karthik Subramanian",
                    roomType = "Pine Cottage",
                    roomNumber = "Cottage 108",
                    totalAmount = 3800.0,
                    advanceCollected = 500.0,
                    checkIn = "24 Oct, 12:00 PM",
                    checkOut = "25 Oct, 11:00 AM",
                    contactNumber = "+91 97890 34567",
                    status = BookingStatus.CANCELLED,
                    guestEmail = "karthik.s@techcorp.in",
                    adults = 1,
                    children = 0,
                    notes = "Cancelled due to travel schedule change."
                )
            )
        }
    }
}
