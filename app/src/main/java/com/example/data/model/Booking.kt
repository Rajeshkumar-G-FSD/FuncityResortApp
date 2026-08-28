package com.example.data.model

import java.util.UUID

enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String): BookingStatus {
            return when (value.trim().lowercase()) {
                "confirmed" -> CONFIRMED
                "pending" -> PENDING
                "cancelled", "canceled" -> CANCELLED
                else -> PENDING
            }
        }
    }
}

data class Booking(
    val id: String = "FC-" + (100000..999999).random().toString(),
    val guestName: String = "",
    val roomType: String = "Couple Room",
    val roomNumber: String = "Room 111",
    val totalAmount: Double = 0.0,
    val advanceCollected: Double = 0.0,
    val checkIn: String = "",
    val checkOut: String = "",
    val contactNumber: String = "",
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val guestEmail: String = "",
    val adults: Int = 2,
    val children: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val pendingAmount: Double
        get() = (totalAmount - advanceCollected).coerceAtLeast(0.0)

    val roomDisplay: String
        get() = "$roomType - $roomNumber"

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "guestName" to guestName,
            "roomType" to roomType,
            "roomNumber" to roomNumber,
            "totalAmount" to totalAmount,
            "advanceCollected" to advanceCollected,
            "checkIn" to checkIn,
            "checkOut" to checkOut,
            "contactNumber" to contactNumber,
            "status" to status.name,
            "guestEmail" to guestEmail,
            "adults" to adults,
            "children" to children,
            "notes" to notes,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, documentId: String = ""): Booking {
            val id = (map["id"] as? String)?.takeIf { it.isNotBlank() } ?: documentId.ifBlank { "FC-" + (100000..999999).random().toString() }
            val guestName = map["guestName"] as? String ?: "Guest"
            val roomType = map["roomType"] as? String ?: "Couple Room"
            val roomNumber = map["roomNumber"] as? String ?: "Room 101"
            val totalAmount = when (val total = map["totalAmount"]) {
                is Number -> total.toDouble()
                is String -> total.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            val advanceCollected = when (val adv = map["advanceCollected"]) {
                is Number -> adv.toDouble()
                is String -> adv.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            val checkIn = map["checkIn"] as? String ?: "Today, 2:00 PM"
            val checkOut = map["checkOut"] as? String ?: "Tomorrow, 11:00 AM"
            val contactNumber = map["contactNumber"] as? String ?: ""
            val statusStr = map["status"] as? String ?: "CONFIRMED"
            val status = try {
                BookingStatus.valueOf(statusStr.uppercase())
            } catch (e: Exception) {
                BookingStatus.fromString(statusStr)
            }
            val guestEmail = map["guestEmail"] as? String ?: ""
            val adults = (map["adults"] as? Number)?.toInt() ?: 2
            val children = (map["children"] as? Number)?.toInt() ?: 0
            val notes = map["notes"] as? String ?: ""
            val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

            return Booking(
                id = id,
                guestName = guestName,
                roomType = roomType,
                roomNumber = roomNumber,
                totalAmount = totalAmount,
                advanceCollected = advanceCollected,
                checkIn = checkIn,
                checkOut = checkOut,
                contactNumber = contactNumber,
                status = status,
                guestEmail = guestEmail,
                adults = adults,
                children = children,
                notes = notes,
                createdAt = createdAt
            )
        }
    }
}
