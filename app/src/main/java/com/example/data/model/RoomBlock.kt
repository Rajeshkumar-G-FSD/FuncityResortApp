package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class RoomInfo(
    val id: String,
    val roomNumber: String,
    val roomType: String,
    val weekdayPrice: Double,
    val weekendPrice: Double
) {
    val cleanNumber: String
        get() = roomNumber.replace("Room ", "").trim()

    val displayName: String
        get() = if (roomNumber.startsWith("Room") || roomNumber.startsWith("Villa") || roomNumber.startsWith("Cottage")) {
            roomNumber
        } else {
            "Room $roomNumber"
        }
}

data class RoomCategory(
    val name: String,
    val weekdayPrice: Double,
    val weekendPrice: Double,
    val rooms: List<RoomInfo>
)

data class BlockedRoom(
    val id: String = "", // e.g. "102_2026-08-30"
    val roomId: String = "", // "102"
    val roomNumber: String = "", // "102" or "Room 102"
    val roomType: String = "", // "Couple Room"
    val date: String = "", // "2026-08-30" (YYYY-MM-DD)
    val reason: String = "Blocked by admin",
    val blockedBy: String = "admin",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id.ifBlank { "${roomId.replace("Room ", "").trim()}_$date" },
            "roomId" to roomId.replace("Room ", "").trim(),
            "roomNumber" to roomNumber,
            "roomType" to roomType,
            "date" to date,
            "reason" to reason,
            "blockedBy" to blockedBy,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, documentId: String = ""): BlockedRoom {
            val date = (map["date"] as? String)
                ?: (map["blockedDate"] as? String)
                ?: (map["blockDate"] as? String)
                ?: ""
            
            val roomId = (map["roomId"] as? String)
                ?: (map["roomNumber"] as? String)
                ?: (map["room_id"] as? String)
                ?: (map["room"] as? String)
                ?: documentId.substringBefore("_")
            
            val roomNumber = (map["roomNumber"] as? String)
                ?: (map["roomId"] as? String)
                ?: roomId

            val roomType = (map["roomType"] as? String)
                ?: (map["room_type"] as? String)
                ?: "Couple Room"

            val reason = (map["reason"] as? String) ?: "Blocked by admin"
            val blockedBy = (map["blockedBy"] as? String) ?: "admin"
            val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val id = documentId.ifBlank { (map["id"] as? String) ?: "${roomId.replace("Room ", "").trim()}_$date" }

            return BlockedRoom(
                id = id,
                roomId = roomId.replace("Room ", "").trim(),
                roomNumber = roomNumber,
                roomType = roomType,
                date = date,
                reason = reason,
                blockedBy = blockedBy,
                createdAt = createdAt
            )
        }
    }
}

enum class RoomSlotStatus {
    AVAILABLE,
    BLOCKED,
    BOOKED_CONFIRMED,
    BOOKED_PENDING,
    PAST
}

data class CalendarDay(
    val date: Date,
    val isoDate: String, // "2026-08-29"
    val dayOfWeek: String, // "SAT"
    val dayOfMonth: String, // "29"
    val monthShort: String, // "Aug" or "Sept"
    val isToday: Boolean,
    val isPast: Boolean
) {
    val displayHeader: String
        get() = "$dayOfWeek $dayOfMonth $monthShort"
}

object RoomDataDefaults {
    val ROOM_CATEGORIES: List<RoomCategory> = listOf(
        RoomCategory(
            name = "Couple Room",
            weekdayPrice = 1500.0,
            weekendPrice = 2000.0,
            rooms = listOf(
                RoomInfo("102", "102", "Couple Room", 1500.0, 2000.0),
                RoomInfo("103", "103", "Couple Room", 1500.0, 2000.0),
                RoomInfo("104", "104", "Couple Room", 1500.0, 2000.0),
                RoomInfo("105", "105", "Couple Room", 1500.0, 2000.0),
                RoomInfo("106", "106", "Couple Room", 1500.0, 2000.0),
                RoomInfo("107", "107", "Couple Room", 1500.0, 2000.0),
                RoomInfo("108", "108", "Couple Room", 1500.0, 2000.0),
                RoomInfo("110", "110", "Couple Room", 1500.0, 2000.0),
                RoomInfo("111", "111", "Couple Room", 1500.0, 2000.0)
            )
        ),
        RoomCategory(
            name = "Family Room",
            weekdayPrice = 3000.0,
            weekendPrice = 3500.0,
            rooms = listOf(
                RoomInfo("109", "109", "Family Room", 3000.0, 3500.0)
            )
        )
    )

    fun getAllRooms(): List<RoomInfo> = ROOM_CATEGORIES.flatMap { it.rooms }

    fun getInitialBlockedRooms(): List<BlockedRoom> {
        val calendar = Calendar.getInstance()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        
        // Let's create blocks for tomorrow/current date matching web screenshot
        val blocks = mutableListOf<BlockedRoom>()
        val todayStr = isoFormat.format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = isoFormat.format(calendar.time)
        
        // Rooms 102-107 blocked on tomorrow / 30th
        listOf("102", "103", "104", "105", "106", "107").forEach { roomId ->
            blocks.add(
                BlockedRoom(
                    id = "${roomId}_$tomorrowStr",
                    roomId = roomId,
                    roomNumber = roomId,
                    roomType = "Couple Room",
                    date = tomorrowStr,
                    reason = "Room maintenance / Blocked by admin"
                )
            )
        }
        return blocks
    }
}
