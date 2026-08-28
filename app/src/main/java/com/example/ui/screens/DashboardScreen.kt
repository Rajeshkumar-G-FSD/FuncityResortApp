package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.ResortMetrics
import com.example.ui.components.BookingCard
import com.example.ui.components.RevenueLineChart
import com.example.ui.components.StatusSplitDonutChart
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    metrics: ResortMetrics,
    bookings: List<Booking>,
    currencySymbol: String,
    onStatusChange: (String, BookingStatus) -> Unit,
    onEditBooking: (Booking) -> Unit,
    onDeleteBooking: (String) -> Unit,
    onNewBooking: () -> Unit,
    onViewAllBookings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
            maximumFractionDigits = 0
        }
    }

    val currentBooking = bookings.firstOrNull { it.status == BookingStatus.CONFIRMED } ?: bookings.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Section 1: KPI Cards Grid
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 600.dp

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "CONFIRMED REVENUE",
                            value = "$currencySymbol${formatter.format(metrics.confirmedRevenue)}",
                            icon = Icons.Default.Payments,
                            iconColor = BrandGreen,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "ADVANCE COLLECTED",
                            value = "$currencySymbol${formatter.format(metrics.advanceCollected)}",
                            icon = Icons.Default.TrendingUp,
                            iconColor = BrandBlue,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "PENDING AMOUNT",
                            value = "$currencySymbol${formatter.format(metrics.pendingAmount)}",
                            subText = "${metrics.pendingCount} awaiting",
                            icon = Icons.Default.PendingActions,
                            iconColor = BrandMustard,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "CANCELLED TODAY",
                            value = "${metrics.cancelledToday}",
                            icon = Icons.Default.Cancel,
                            iconColor = BrandRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiCard(
                                title = "Confirmed Revenue",
                                value = "$currencySymbol${formatter.format(metrics.confirmedRevenue)}",
                                icon = Icons.Default.TrendingUp,
                                iconColor = BrandGreen,
                                valueColor = BrandGreen,
                                modifier = Modifier.weight(1f).testTag("kpi_confirmed_revenue")
                            )
                            KpiCard(
                                title = "Advance Collected",
                                value = "$currencySymbol${formatter.format(metrics.advanceCollected)}",
                                icon = Icons.Default.Payments,
                                iconColor = BrandBlue,
                                valueColor = BrandBlue,
                                modifier = Modifier.weight(1f).testTag("kpi_advance_collected")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiCard(
                                title = "Pending Amount",
                                value = "$currencySymbol${formatter.format(metrics.pendingAmount)}",
                                subText = "${metrics.pendingCount} awaiting",
                                icon = Icons.Default.PendingActions,
                                iconColor = BrandMustard,
                                valueColor = BrandMustard,
                                modifier = Modifier.weight(1f).testTag("kpi_pending_amount")
                            )
                            KpiCard(
                                title = "Cancelled Today",
                                value = "${metrics.cancelledToday}",
                                icon = Icons.Default.Cancel,
                                iconColor = BrandRed,
                                valueColor = BrandRed,
                                modifier = Modifier.weight(1f).testTag("kpi_cancelled_today")
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Analytics Header & Charts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceText
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWide = maxWidth >= 600.dp
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RevenueLineChart(
                                history = metrics.revenueHistory,
                                modifier = Modifier.weight(1.3f).height(190.dp)
                            )
                            StatusSplitDonutChart(
                                metrics = metrics,
                                modifier = Modifier.weight(1f).height(190.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RevenueLineChart(
                                history = metrics.revenueHistory,
                                modifier = Modifier.fillMaxWidth().height(180.dp)
                            )
                            StatusSplitDonutChart(
                                metrics = metrics,
                                modifier = Modifier.fillMaxWidth().height(180.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Current Booking
        if (currentBooking != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Booking",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceText
                        )

                        Text(
                            text = "View all (${bookings.size})",
                            color = PrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    BookingCard(
                        booking = currentBooking,
                        currencySymbol = currencySymbol,
                        onStatusChange = { newStatus ->
                            onStatusChange(currentBooking.id, newStatus)
                        },
                        onEdit = { onEditBooking(currentBooking) },
                        onDelete = { onDeleteBooking(currentBooking.id) }
                    )
                }
            }
        }

        // Section 4: Quick Actions & Other Bookings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Reservations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceText
                )

                Button(
                    onClick = onNewBooking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGold,
                        contentColor = OnBrandGold
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("dashboard_add_booking_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("New Booking", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Other bookings list (skipping current)
        val otherBookings = bookings.filter { it.id != currentBooking?.id }
        if (otherBookings.isNotEmpty()) {
            items(otherBookings.take(3), key = { it.id }) { booking ->
                BookingCard(
                    booking = booking,
                    currencySymbol = currencySymbol,
                    onStatusChange = { newStatus -> onStatusChange(booking.id, newStatus) },
                    onEdit = { onEditBooking(booking) },
                    onDelete = { onDeleteBooking(booking.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    valueColor: Color = OnSurfaceText,
    subText: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(12.dp)),
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.04.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )

                if (subText != null) {
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
