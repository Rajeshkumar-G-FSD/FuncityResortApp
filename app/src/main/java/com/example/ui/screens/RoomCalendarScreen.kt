package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedRoom
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.CalendarDay
import com.example.data.model.RoomCategory
import com.example.data.model.RoomDataDefaults
import com.example.data.model.RoomInfo
import com.example.data.model.RoomSlotStatus
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandMustard
import com.example.ui.theme.BrandRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnBrandGold
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.WarmBackground
import com.example.ui.theme.WarmSurface
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomCalendarScreen(
    blockedRooms: List<BlockedRoom>,
    bookings: List<Booking>,
    startOffsetDays: Int,
    currencySymbol: String,
    onToggleBlock: (roomId: String, isoDate: String, roomType: String) -> Unit,
    onBatchBlockRoom: (roomId: String, dates: List<String>, roomType: String) -> Unit,
    onBatchUnblockRoom: (roomId: String, dates: List<String>) -> Unit,
    onNextDays: () -> Unit,
    onPrevDays: () -> Unit,
    onJumpToToday: () -> Unit,
    onSwitchToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate calendar days for 21-day window matching web screenshot
    val visibleDaysCount = 21
    val calendarDays = remember(startOffsetDays) {
        val list = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        val todayCal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, startOffsetDays)

        val isoFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dowFormatter = SimpleDateFormat("EEE", Locale.ENGLISH)
        val domFormatter = SimpleDateFormat("d", Locale.ENGLISH)
        val monthFormatter = SimpleDateFormat("MMM", Locale.ENGLISH)

        val todayIso = isoFormatter.format(todayCal.time)

        for (i in 0 until visibleDaysCount) {
            val date = cal.time
            val iso = isoFormatter.format(date)
            val dow = dowFormatter.format(date).uppercase(Locale.ENGLISH)
            val dom = domFormatter.format(date)
            val month = monthFormatter.format(date)

            val isToday = iso == todayIso
            val isPast = cal.before(todayCal) && !isToday

            list.add(
                CalendarDay(
                    date = date,
                    isoDate = iso,
                    dayOfWeek = dow,
                    dayOfMonth = dom,
                    monthShort = month,
                    isToday = isToday,
                    isPast = isPast
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val rangeLabel = remember(calendarDays) {
        if (calendarDays.isEmpty()) ""
        else {
            val start = calendarDays.first()
            val end = calendarDays.last()
            val format = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            "${format.format(start.date)} – ${format.format(end.date)}"
        }
    }

    // Room categories
    val categories = remember { RoomDataDefaults.ROOM_CATEGORIES }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredCategories = remember(selectedCategory) {
        if (selectedCategory == "All") categories
        else categories.filter { it.name == selectedCategory }
    }

    // Dialog state for viewing/editing slot details
    var activeSlotModal by remember { mutableStateOf<SlotInfoModalData?>(null) }
    var batchActionRoom by remember { mutableStateOf<RoomInfo?>(null) }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
            maximumFractionDigits = 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. Top Bar Segment Switcher & Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Segmented Switcher: Dashboard vs Room Calendar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSwitchToDashboard() },
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Dashboard",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dashboard",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)),
                        color = PrimaryDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Room calendar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Room calendar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Quick Category Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("All", "Couple", "Family").forEach { catKey ->
                    val isSelected = (catKey == "All" && selectedCategory == "All") ||
                            (catKey == "Couple" && selectedCategory == "Couple Room") ||
                            (catKey == "Family" && selectedCategory == "Family Room")

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedCategory = when (catKey) {
                                    "Couple" -> "Couple Room"
                                    "Family" -> "Family Room"
                                    else -> "All"
                                }
                            },
                        color = if (isSelected) BrandGold else SurfaceContainerLowest,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSelected) BrandGold else CardBorder)
                    ) {
                        Text(
                            text = catKey,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnBrandGold else OnSurfaceText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 2. Room block calendar Header & Range Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Room block calendar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceText
                        )
                    }

                    // Navigation Range Controls (< range > Today)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onPrevDays() }
                                .testTag("btn_prev_range"),
                            shape = CircleShape,
                            color = WarmSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous days",
                                    tint = OnSurfaceText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = rangeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceText,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onNextDays() }
                                .testTag("btn_next_range"),
                            shape = CircleShape,
                            color = WarmSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next days",
                                    tint = OnSurfaceText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onJumpToToday() }
                                .testTag("btn_today"),
                            shape = RoundedCornerShape(16.dp),
                            color = WarmSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Legend Row matching screenshot exactly
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LegendItem(
                        color = Color.White,
                        borderColor = CardBorder,
                        label = "Available — click to block"
                    )
                    LegendItem(
                        color = BrandRed,
                        borderColor = BrandRed,
                        icon = Icons.Default.Lock,
                        label = "Blocked — click to unblock"
                    )
                    LegendItem(
                        color = BrandBlue,
                        borderColor = BrandBlue,
                        isDot = true,
                        label = "Confirmed booking"
                    )
                    LegendItem(
                        color = BrandMustard,
                        borderColor = BrandMustard,
                        label = "Pending booking"
                    )
                    LegendItem(
                        color = Color(0xFFE5E7EB),
                        borderColor = Color(0xFFD1D5DB),
                        label = "Past"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Main Interactive Calendar Grid
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = SurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Outer Layout: Fixed Room Column on Left + Horizontally Scrollable Dates Grid on Right
                Row(modifier = Modifier.fillMaxWidth()) {
                    
                    // ================= LEFT COLUMN: Fixed ROOM headers & room labels =================
                    Column(
                        modifier = Modifier
                            .width(88.dp)
                            .background(SurfaceContainerLowest)
                    ) {
                        // Top left corner header "ROOM"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(WarmSurface)
                                .border(BorderStroke(0.5.dp, CardBorder))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "ROOM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Categories and Rooms
                        filteredCategories.forEach { category ->
                            // Category Row Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .background(Color(0xFFF9FAFB))
                                    .border(BorderStroke(0.5.dp, CardBorder))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = category.name.take(6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Room rows
                            category.rooms.forEach { room ->
                                val visibleIsoDates = calendarDays.map { it.isoDate }
                                val roomCleanId = room.cleanNumber
                                val isAllBlocked = visibleIsoDates.all { dateIso ->
                                    blockedRooms.any { b -> b.roomId.replace("Room ", "").trim() == roomCleanId && b.date == dateIso }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(SurfaceContainerLowest)
                                        .border(BorderStroke(0.5.dp, CardBorder))
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = room.cleanNumber,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurfaceText
                                    )

                                    // Quick lock/unlock action button on room row
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Surface(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    batchActionRoom = room
                                                },
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.Transparent,
                                            border = BorderStroke(0.5.dp, CardBorder)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isAllBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = "Quick actions for Room ${room.cleanNumber}",
                                                    tint = if (isAllBlocked) BrandGreen else OnSurfaceVariant,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= RIGHT COLUMN: Horizontally Scrollable Calendar Days Grid =================
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Top Header: Date Columns (Day of week, Day number, Month)
                        Row(modifier = Modifier.height(56.dp)) {
                            calendarDays.forEach { day ->
                                val isToday = day.isToday
                                Column(
                                    modifier = Modifier
                                        .width(44.dp)
                                        .fillMaxHeight()
                                        .background(if (isToday) Color(0xFFEFF6FF) else WarmSurface)
                                        .border(BorderStroke(0.5.dp, CardBorder))
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.dayOfWeek,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isToday) BrandBlue else OnSurfaceVariant
                                    )
                                    Text(
                                        text = day.dayOfMonth,
                                        fontSize = 13.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isToday) BrandBlue else OnSurfaceText
                                    )
                                    Text(
                                        text = day.monthShort,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isToday) BrandBlue else OnSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Categories and Room Rows in the Grid
                        filteredCategories.forEach { category ->
                            // Category Row spanning across the grid with pricing
                            Box(
                                modifier = Modifier
                                    .width(44.dp * calendarDays.size)
                                    .height(28.dp)
                                    .background(Color(0xFFF9FAFB))
                                    .border(BorderStroke(0.5.dp, CardBorder))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${category.name} · ${currencySymbol}${currencyFormatter.format(category.weekdayPrice)}/${currencySymbol}${currencyFormatter.format(category.weekendPrice)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                            }

                            // Room rows and their interactive date slots
                            category.rooms.forEach { room ->
                                Row(
                                    modifier = Modifier
                                        .width(44.dp * calendarDays.size)
                                        .height(48.dp)
                                ) {
                                    calendarDays.forEach { day ->
                                        val slotStatus = calculateSlotStatus(
                                            room = room,
                                            day = day,
                                            blockedRooms = blockedRooms,
                                            bookings = bookings
                                        )

                                        val matchingBooking = findBookingForSlot(room, day, bookings)
                                        val matchingBlock = findBlockForSlot(room, day, blockedRooms)

                                        RoomCalendarCell(
                                            status = slotStatus,
                                            day = day,
                                            room = room,
                                            onClick = {
                                                when (slotStatus) {
                                                    RoomSlotStatus.AVAILABLE -> {
                                                        // Toggle block immediately
                                                        onToggleBlock(room.cleanNumber, day.isoDate, room.roomType)
                                                    }
                                                    RoomSlotStatus.BLOCKED -> {
                                                        // Toggle unblock immediately
                                                        onToggleBlock(room.cleanNumber, day.isoDate, room.roomType)
                                                    }
                                                    RoomSlotStatus.BOOKED_CONFIRMED,
                                                    RoomSlotStatus.BOOKED_PENDING -> {
                                                        // Open booking modal
                                                        activeSlotModal = SlotInfoModalData(
                                                            room = room,
                                                            day = day,
                                                            status = slotStatus,
                                                            booking = matchingBooking,
                                                            blockedRoom = matchingBlock
                                                        )
                                                    }
                                                    RoomSlotStatus.PAST -> {
                                                        // Informative tap
                                                        activeSlotModal = SlotInfoModalData(
                                                            room = room,
                                                            day = day,
                                                            status = slotStatus,
                                                            booking = matchingBooking,
                                                            blockedRoom = matchingBlock
                                                        )
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                activeSlotModal = SlotInfoModalData(
                                                    room = room,
                                                    day = day,
                                                    status = slotStatus,
                                                    booking = matchingBooking,
                                                    blockedRoom = matchingBlock
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Informative Note matching screenshot
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(WarmSurface, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Blocking a room hides that room number from guests for the selected nights. Online bookings (confirmed / pending) are shown here but can only be changed from the Dashboard.",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal: Detailed info / Action Dialog
    activeSlotModal?.let { modalData ->
        SlotDetailDialog(
            data = modalData,
            currencySymbol = currencySymbol,
            onDismiss = { activeSlotModal = null },
            onToggleBlock = {
                onToggleBlock(modalData.room.cleanNumber, modalData.day.isoDate, modalData.room.roomType)
                activeSlotModal = null
            }
        )
    }

    // Modal: Batch block / unblock options for a room
    batchActionRoom?.let { room ->
        val visibleDates = calendarDays.map { it.isoDate }
        val cleanRoomId = room.cleanNumber
        val blockedDatesInVisible = blockedRooms.filter { 
            it.roomId.replace("Room ", "").trim() == cleanRoomId && visibleDates.contains(it.date) 
        }.map { it.date }

        AlertDialog(
            onDismissRequest = { batchActionRoom = null },
            title = {
                Text(
                    text = "Room ${room.cleanNumber} Bulk Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Range: $rangeLabel (${visibleDaysCount} days)",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Currently blocked: ${blockedDatesInVisible.size} / ${visibleDaysCount} days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (blockedDatesInVisible.isNotEmpty()) BrandRed else OnSurfaceText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quickly block or unblock all unbooked slots in this visible range for Room ${room.cleanNumber}.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchBlockRoom(room.cleanNumber, visibleDates, room.roomType)
                        batchActionRoom = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Block All Visible", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onBatchUnblockRoom(room.cleanNumber, visibleDates)
                        batchActionRoom = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unblock All", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun RoomCalendarCell(
    status: RoomSlotStatus,
    day: CalendarDay,
    room: RoomInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = when (status) {
        RoomSlotStatus.AVAILABLE -> Color.White
        RoomSlotStatus.BLOCKED -> BrandRed
        RoomSlotStatus.BOOKED_CONFIRMED -> BrandBlue
        RoomSlotStatus.BOOKED_PENDING -> BrandMustard
        RoomSlotStatus.PAST -> Color(0xFFF3F4F6)
    }

    val borderColor = when (status) {
        RoomSlotStatus.AVAILABLE -> CardBorder
        RoomSlotStatus.BLOCKED -> BrandRed
        RoomSlotStatus.BOOKED_CONFIRMED -> BrandBlue
        RoomSlotStatus.BOOKED_PENDING -> BrandMustard
        RoomSlotStatus.PAST -> Color(0xFFE5E7EB)
    }

    Box(
        modifier = Modifier
            .width(44.dp)
            .fillMaxHeight()
            .background(bgColor)
            .border(BorderStroke(0.5.dp, borderColor))
            .clickable(onClick = onClick)
            .testTag("cell_${room.cleanNumber}_${day.isoDate}"),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            RoomSlotStatus.BLOCKED -> {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Blocked",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            RoomSlotStatus.BOOKED_CONFIRMED -> {
                // Circle dot icon matching screenshot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
            }
            RoomSlotStatus.BOOKED_PENDING -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
            }
            RoomSlotStatus.AVAILABLE -> {
                // Empty white cell
            }
            RoomSlotStatus.PAST -> {
                // Empty gray cell
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    borderColor: Color,
    label: String,
    icon: ImageVector? = null,
    isDot: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            } else if (isDot) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = OnSurfaceText,
            fontWeight = FontWeight.Medium
        )
    }
}

data class SlotInfoModalData(
    val room: RoomInfo,
    val day: CalendarDay,
    val status: RoomSlotStatus,
    val booking: Booking? = null,
    val blockedRoom: BlockedRoom? = null
)

@Composable
private fun SlotDetailDialog(
    data: SlotInfoModalData,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val statusTitle = when (data.status) {
        RoomSlotStatus.AVAILABLE -> "Available Room"
        RoomSlotStatus.BLOCKED -> "Room Blocked"
        RoomSlotStatus.BOOKED_CONFIRMED -> "Confirmed Booking"
        RoomSlotStatus.BOOKED_PENDING -> "Pending Booking"
        RoomSlotStatus.PAST -> "Past Date"
    }

    val statusColor = when (data.status) {
        RoomSlotStatus.AVAILABLE -> BrandGreen
        RoomSlotStatus.BLOCKED -> BrandRed
        RoomSlotStatus.BOOKED_CONFIRMED -> BrandBlue
        RoomSlotStatus.BOOKED_PENDING -> BrandMustard
        RoomSlotStatus.PAST -> OnSurfaceVariant
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Room ${data.room.cleanNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceText
                    )
                    Text(
                        text = "${data.room.roomType} · ${data.day.displayHeader}",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (data.booking != null) {
                    val b = data.booking
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = WarmSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Booking ID:", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(text = b.id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Guest Name:", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(text = b.guestName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Contact:", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(text = b.contactNumber, fontSize = 12.sp, color = OnSurfaceText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Check-in / Out:", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(text = "${b.checkIn} → ${b.checkOut}", fontSize = 11.sp, color = OnSurfaceText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Amount Paid / Total:", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(
                                    text = "$currencySymbol${b.advanceCollected.toInt()} / $currencySymbol${b.totalAmount.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen
                                )
                            }
                        }
                    }
                    Text(
                        text = "Online bookings can be managed from the Bookings tab & Dashboard.",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant
                    )
                } else if (data.status == RoomSlotStatus.BLOCKED) {
                    Text(
                        text = "This room is currently blocked for this date and hidden from guest reservation searches.",
                        fontSize = 13.sp,
                        color = OnSurfaceText
                    )
                    data.blockedRoom?.reason?.let { reason ->
                        Text(
                            text = "Reason: $reason",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "This room is currently available for guest reservations on ${data.day.displayHeader}.",
                        fontSize = 13.sp,
                        color = OnSurfaceText
                    )
                    Text(
                        text = "Tap 'Block Room' below to instantly lock this room in Firebase and prevent online bookings.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (data.status == RoomSlotStatus.AVAILABLE) {
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Block Room", fontWeight = FontWeight.Bold)
                }
            } else if (data.status == RoomSlotStatus.BLOCKED) {
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unblock Room", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            if (data.status == RoomSlotStatus.AVAILABLE || data.status == RoomSlotStatus.BLOCKED) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

private fun calculateSlotStatus(
    room: RoomInfo,
    day: CalendarDay,
    blockedRooms: List<BlockedRoom>,
    bookings: List<Booking>
): RoomSlotStatus {
    val cleanRoomNum = room.cleanNumber

    // 1. Check if booked
    val booking = bookings.firstOrNull { b ->
        val bRoom = b.roomNumber.replace("Room ", "").trim()
        if (bRoom == cleanRoomNum && b.status != BookingStatus.CANCELLED) {
            isDateInBookingRange(day.isoDate, b.checkIn, b.checkOut)
        } else false
    }

    if (booking != null) {
        return if (booking.status == BookingStatus.CONFIRMED) RoomSlotStatus.BOOKED_CONFIRMED
        else RoomSlotStatus.BOOKED_PENDING
    }

    // 2. Check if blocked
    val isBlocked = blockedRooms.any { b ->
        val bRoom = b.roomId.replace("Room ", "").trim()
        bRoom == cleanRoomNum && b.date == day.isoDate
    }

    if (isBlocked) {
        return RoomSlotStatus.BLOCKED
    }

    // 3. Check past
    if (day.isPast) {
        return RoomSlotStatus.PAST
    }

    return RoomSlotStatus.AVAILABLE
}

private fun findBookingForSlot(
    room: RoomInfo,
    day: CalendarDay,
    bookings: List<Booking>
): Booking? {
    val cleanRoomNum = room.cleanNumber
    return bookings.firstOrNull { b ->
        val bRoom = b.roomNumber.replace("Room ", "").trim()
        bRoom == cleanRoomNum && b.status != BookingStatus.CANCELLED && isDateInBookingRange(day.isoDate, b.checkIn, b.checkOut)
    }
}

private fun findBlockForSlot(
    room: RoomInfo,
    day: CalendarDay,
    blockedRooms: List<BlockedRoom>
): BlockedRoom? {
    val cleanRoomNum = room.cleanNumber
    return blockedRooms.firstOrNull { b ->
        val bRoom = b.roomId.replace("Room ", "").trim()
        bRoom == cleanRoomNum && b.date == day.isoDate
    }
}

private fun isDateInBookingRange(isoDate: String, checkIn: String, checkOut: String): Boolean {
    val target = parseAnyDate(isoDate) ?: return false
    val start = parseAnyDate(checkIn) ?: return false
    val end = parseAnyDate(checkOut) ?: return false

    // Date range is [start, end)
    val targetTime = target.time
    val startTime = start.time
    val endTime = end.time

    return targetTime in startTime until endTime || (targetTime == startTime)
}

private fun parseAnyDate(str: String): Date? {
    val clean = str.split(",").firstOrNull()?.trim() ?: str.trim()
    val formats = listOf(
        "yyyy-MM-dd",
        "MMM d, yyyy",
        "MMM d yyyy",
        "d MMM yyyy",
        "MMM d",
        "d MMM",
        "dd/MM/yyyy",
        "yyyy/MM/dd"
    )

    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
            sdf.isLenient = true
            val d = sdf.parse(clean)
            if (d != null) {
                // If year wasn't present, set to current year
                if (!fmt.contains("yyyy")) {
                    val cal = Calendar.getInstance()
                    val year = cal.get(Calendar.YEAR)
                    cal.time = d
                    cal.set(Calendar.YEAR, year)
                    return cal.time
                }
                return d
            }
        } catch (_: Exception) {
        }
    }
    return null
}
