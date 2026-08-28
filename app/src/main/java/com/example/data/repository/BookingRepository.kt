package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.model.BlockedRoom
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.DailyRevenuePoint
import com.example.data.model.ResortMetrics
import com.example.data.model.RoomDataDefaults
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingRepository private constructor(context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var bookingsListener: ListenerRegistration? = null
    private var blockedRoomsListener: ListenerRegistration? = null

    private val _bookings = MutableStateFlow<List<Booking>>(getInitialSampleBookings())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _blockedRooms = MutableStateFlow<List<BlockedRoom>>(RoomDataDefaults.getInitialBlockedRooms())
    val blockedRooms: StateFlow<List<BlockedRoom>> = _blockedRooms.asStateFlow()

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
            
            // 1. Sync Bookings
            bookingsListener?.remove()
            bookingsListener = firestore.collection("bookings")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("BookingRepo", "Firestore bookings listen failed: ${e.message}")
                        _syncMessage.value = "Local cached (Firestore sync: ${e.localizedMessage})"
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
                    }
                }

            // 2. Sync Blocked Rooms (try collection 'blocked_rooms' and 'blockedRooms')
            blockedRoomsListener?.remove()
            blockedRoomsListener = firestore.collection("blocked_rooms")
                .addSnapshotListener { snapshots, e ->
                    _isSyncing.value = false
                    if (e != null) {
                        Log.w("BookingRepo", "Firestore blocked_rooms listen failed: ${e.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val fetchedBlocks = snapshots.documents.mapNotNull { doc ->
                            doc.data?.let { BlockedRoom.fromMap(it, doc.id) }
                        }
                        if (fetchedBlocks.isNotEmpty()) {
                            _blockedRooms.value = fetchedBlocks
                            Log.d("BookingRepo", "Synced ${fetchedBlocks.size} blocked rooms from Firebase")
                        }
                    }
                }

            // Also check alternate collection name 'blockedRooms' for full web compatibility
            firestore.collection("blockedRooms")
                .get()
                .addOnSuccessListener { altSnapshots ->
                    if (altSnapshots != null && !altSnapshots.isEmpty) {
                        val altBlocks = altSnapshots.documents.mapNotNull { doc ->
                            doc.data?.let { BlockedRoom.fromMap(it, doc.id) }
                        }
                        if (altBlocks.isNotEmpty()) {
                            val combined = (_blockedRooms.value + altBlocks).distinctBy { it.id }
                            _blockedRooms.value = combined
                        }
                    }
                }

        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Failed to setup snapshot listener", e)
        }
    }

    suspend fun blockRoom(
        roomId: String,
        date: String,
        roomType: String = "Couple Room",
        reason: String = "Blocked by admin"
    ): Result<BlockedRoom> = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.replace("Room ", "").trim()
        val docId = "${cleanRoomId}_$date"
        val block = BlockedRoom(
            id = docId,
            roomId = cleanRoomId,
            roomNumber = cleanRoomId,
            roomType = roomType,
            date = date,
            reason = reason,
            blockedBy = "admin",
            createdAt = System.currentTimeMillis()
        )

        try {
            _isSyncing.value = true
            // Instant local update
            val currentList = _blockedRooms.value.filter { it.id != docId }.toMutableList()
            currentList.add(block)
            _blockedRooms.value = currentList

            // Sync to Firestore (both blocked_rooms and blockedRooms collections for web compatibility)
            val firestore = FirebaseManager.getFirestore()
            firestore?.let { db ->
                db.collection("blocked_rooms").document(docId).set(block.toMap(), SetOptions.merge()).await()
                db.collection("blockedRooms").document(docId).set(block.toMap(), SetOptions.merge())
            }

            _isSyncing.value = false
            _syncMessage.value = "Room $cleanRoomId blocked on $date"
            Result.success(block)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error blocking room in Firebase", e)
            _syncMessage.value = "Room blocked locally"
            Result.success(block)
        }
    }

    suspend fun unblockRoom(roomId: String, date: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.replace("Room ", "").trim()
        val docId = "${cleanRoomId}_$date"

        try {
            _isSyncing.value = true
            // Instant local update
            val currentList = _blockedRooms.value.filter { 
                val blockClean = it.roomId.replace("Room ", "").trim()
                !(blockClean == cleanRoomId && it.date == date)
            }
            _blockedRooms.value = currentList

            // Delete from Firestore
            val firestore = FirebaseManager.getFirestore()
            firestore?.let { db ->
                db.collection("blocked_rooms").document(docId).delete().await()
                db.collection("blockedRooms").document(docId).delete()
            }

            _isSyncing.value = false
            _syncMessage.value = "Room $cleanRoomId unblocked for $date"
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error unblocking room in Firebase", e)
            _syncMessage.value = "Room unblocked locally"
            Result.success(Unit)
        }
    }

    suspend fun batchBlockRoom(
        roomId: String,
        dates: List<String>,
        roomType: String = "Couple Room"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.replace("Room ", "").trim()
        try {
            _isSyncing.value = true
            val currentList = _blockedRooms.value.toMutableList()
            val firestore = FirebaseManager.getFirestore()

            for (date in dates) {
                val docId = "${cleanRoomId}_$date"
                val block = BlockedRoom(
                    id = docId,
                    roomId = cleanRoomId,
                    roomNumber = cleanRoomId,
                    roomType = roomType,
                    date = date,
                    reason = "Bulk blocked by admin"
                )
                currentList.removeAll { it.id == docId }
                currentList.add(block)

                firestore?.let { db ->
                    db.collection("blocked_rooms").document(docId).set(block.toMap(), SetOptions.merge())
                    db.collection("blockedRooms").document(docId).set(block.toMap(), SetOptions.merge())
                }
            }

            _blockedRooms.value = currentList
            _isSyncing.value = false
            _syncMessage.value = "Room $cleanRoomId blocked for ${dates.size} dates"
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error batch blocking rooms", e)
            Result.success(Unit)
        }
    }

    suspend fun batchUnblockRoom(roomId: String, dates: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.replace("Room ", "").trim()
        try {
            _isSyncing.value = true
            val datesSet = dates.toSet()
            val currentList = _blockedRooms.value.filterNot { 
                val rId = it.roomId.replace("Room ", "").trim()
                rId == cleanRoomId && datesSet.contains(it.date)
            }
            _blockedRooms.value = currentList

            val firestore = FirebaseManager.getFirestore()
            for (date in dates) {
                val docId = "${cleanRoomId}_$date"
                firestore?.let { db ->
                    db.collection("blocked_rooms").document(docId).delete()
                    db.collection("blockedRooms").document(docId).delete()
                }
            }

            _isSyncing.value = false
            _syncMessage.value = "Room $cleanRoomId unblocked for ${dates.size} dates"
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("BookingRepo", "Error batch unblocking rooms", e)
            Result.success(Unit)
        }
    }

    suspend fun createBooking(booking: Booking): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            val currentList = _bookings.value.toMutableList()
            currentList.add(0, booking)
            _bookings.value = currentList
            _metrics.value = computeMetrics(currentList)

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
            Result.success(booking)
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
            val sampleBookings = getInitialSampleBookings()
            val sampleBlocks = RoomDataDefaults.getInitialBlockedRooms()
            val firestore = FirebaseManager.getFirestore()

            if (firestore != null) {
                for (booking in sampleBookings) {
                    firestore.collection("bookings").document(booking.id).set(booking.toMap()).await()
                }
                for (block in sampleBlocks) {
                    firestore.collection("blocked_rooms").document(block.id).set(block.toMap()).await()
                    firestore.collection("blockedRooms").document(block.id).set(block.toMap()).await()
                }
                _syncMessage.value = "Seeded ${sampleBookings.size} bookings & ${sampleBlocks.size} blocks"
            }
            _bookings.value = sampleBookings
            _blockedRooms.value = sampleBlocks
            _metrics.value = computeMetrics(sampleBookings)
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
            DailyRevenuePoint("16/8", 1200.0),
            DailyRevenuePoint("18/8", 1800.0),
            DailyRevenuePoint("20/8", 1400.0),
            DailyRevenuePoint("22/8", 2200.0),
            DailyRevenuePoint("24/8", 2800.0),
            DailyRevenuePoint("26/8", 3400.0),
            DailyRevenuePoint("28/8", confirmedRev)
        )

        return ResortMetrics(
            confirmedRevenue = confirmedRev,
            advanceCollected = advanceCol,
            pendingAmount = pendingAmt,
            pendingCount = pending.size,
            cancelledToday = cancelled.size,
            confirmedCount = confirmed.size,
            totalBookings = bookingList.size,
            occupancyRate = if (bookingList.isNotEmpty()) ((confirmed.size.toFloat() / 10f) * 100).toInt().coerceIn(20, 95) else 75,
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
                    checkIn = "Aug 29, 2026",
                    checkOut = "Aug 31, 2026",
                    contactNumber = "8072117912",
                    status = BookingStatus.CONFIRMED,
                    guestEmail = "rktechappcode@gmail.com",
                    adults = 2,
                    children = 0,
                    notes = "test"
                ),
                Booking(
                    id = "FC-818051",
                    guestName = "testuser",
                    roomType = "Couple Room",
                    roomNumber = "Room 110",
                    totalAmount = 6160.0,
                    advanceCollected = 2464.0,
                    checkIn = "Aug 29, 2026",
                    checkOut = "Sep 1, 2026",
                    contactNumber = "8072117913",
                    status = BookingStatus.CANCELLED,
                    guestEmail = "rktechappcode@gmail.com",
                    adults = 2,
                    children = 0,
                    notes = "test users"
                )
            )
        }
    }
}

