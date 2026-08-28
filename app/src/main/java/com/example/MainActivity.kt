package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BookingFormDialog
import com.example.ui.components.FuncityBottomNav
import com.example.ui.components.FuncityTopBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RoomCalendarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmBackground
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.ResortViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ResortViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FuncityResortApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FuncityResortApp(viewModel: ResortViewModel) {
    val context = LocalContext.current

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val blockedRooms by viewModel.blockedRooms.collectAsStateWithLifecycle()
    val filteredBookings by viewModel.filteredBookings.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val isFormOpen by viewModel.isBookingFormOpen.collectAsStateWithLifecycle()
    val editingBooking by viewModel.editingBooking.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val calendarStartOffsetDays by viewModel.calendarStartOffsetDays.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { viewModel.login("admin@funcity.com", "password123") }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmBackground),
            containerColor = WarmBackground,
            topBar = {
                FuncityTopBar(
                    isSyncing = isSyncing,
                    onRefresh = { viewModel.refresh() },
                    onSignOut = { viewModel.logout() }
                )
            },
            bottomBar = {
                FuncityBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    AppNavTab.DASHBOARD -> {
                        DashboardScreen(
                            metrics = metrics,
                            bookings = bookings,
                            currencySymbol = currencySymbol,
                            onStatusChange = { id, status -> viewModel.updateBookingStatus(id, status) },
                            onEditBooking = { booking -> viewModel.openEditBookingForm(booking) },
                            onDeleteBooking = { id -> viewModel.deleteBooking(id) },
                            onNewBooking = { viewModel.openNewBookingForm() },
                            onViewAllBookings = { viewModel.selectTab(AppNavTab.BOOKINGS) },
                            onSwitchToCalendar = { viewModel.selectTab(AppNavTab.ROOM_CALENDAR) }
                        )
                    }
                    AppNavTab.ROOM_CALENDAR -> {
                        RoomCalendarScreen(
                            blockedRooms = blockedRooms,
                            bookings = bookings,
                            startOffsetDays = calendarStartOffsetDays,
                            currencySymbol = currencySymbol,
                            onToggleBlock = { roomId, isoDate, roomType ->
                                viewModel.toggleRoomBlock(roomId, isoDate, roomType)
                            },
                            onBatchBlockRoom = { roomId, dates, roomType ->
                                viewModel.blockRoomForVisibleRange(roomId, dates, roomType)
                            },
                            onBatchUnblockRoom = { roomId, dates ->
                                viewModel.unblockRoomForVisibleRange(roomId, dates)
                            },
                            onNextDays = { viewModel.nextCalendarDays(14) },
                            onPrevDays = { viewModel.prevCalendarDays(14) },
                            onJumpToToday = { viewModel.jumpCalendarToToday() },
                            onSwitchToDashboard = { viewModel.selectTab(AppNavTab.DASHBOARD) }
                        )
                    }
                    AppNavTab.BOOKINGS -> {
                        BookingsScreen(
                            bookings = filteredBookings,
                            searchQuery = searchQuery,
                            selectedStatus = statusFilter,
                            currencySymbol = currencySymbol,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onStatusFilterChange = { viewModel.setStatusFilter(it) },
                            onStatusChange = { id, status -> viewModel.updateBookingStatus(id, status) },
                            onEditBooking = { booking -> viewModel.openEditBookingForm(booking) },
                            onDeleteBooking = { id -> viewModel.deleteBooking(id) },
                            onNewBooking = { viewModel.openNewBookingForm() }
                        )
                    }
                    AppNavTab.ANALYTICS -> {
                        AnalyticsScreen(
                            metrics = metrics,
                            bookings = bookings,
                            currencySymbol = currencySymbol
                        )
                    }
                    AppNavTab.SETTINGS -> {
                        SettingsScreen(
                            isSyncing = isSyncing,
                            onSeedData = { viewModel.seedData() },
                            onRefresh = { viewModel.refresh() },
                            onSignOut = { viewModel.logout() }
                        )
                    }
                }

                // Create / Edit Booking Form Dialog
                if (isFormOpen) {
                    BookingFormDialog(
                        initialBooking = editingBooking,
                        currencySymbol = currencySymbol,
                        onDismiss = { viewModel.closeBookingForm() },
                        onSave = { booking -> viewModel.saveBooking(booking) }
                    )
                }
            }
        }
    }
}
