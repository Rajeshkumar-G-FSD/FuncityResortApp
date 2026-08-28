package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.ui.components.BookingCard
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

@Composable
fun BookingsScreen(
    bookings: List<Booking>,
    searchQuery: String,
    selectedStatus: BookingStatus?,
    currencySymbol: String,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (BookingStatus?) -> Unit,
    onStatusChange: (String, BookingStatus) -> Unit,
    onEditBooking: (Booking) -> Unit,
    onDeleteBooking: (String) -> Unit,
    onNewBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = WarmBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewBooking,
                containerColor = BrandGold,
                contentColor = OnBrandGold,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_booking")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Booking", modifier = Modifier.size(24.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by Guest, Booking ID, Room...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("bookings_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLowest,
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChipPill(
                        label = "All (${bookings.size})",
                        isSelected = selectedStatus == null,
                        onClick = { onStatusFilterChange(null) }
                    )
                }

                item {
                    FilterChipPill(
                        label = "Confirmed",
                        accentColor = BrandGreen,
                        isSelected = selectedStatus == BookingStatus.CONFIRMED,
                        onClick = { onStatusFilterChange(BookingStatus.CONFIRMED) }
                    )
                }

                item {
                    FilterChipPill(
                        label = "Pending",
                        accentColor = BrandMustard,
                        isSelected = selectedStatus == BookingStatus.PENDING,
                        onClick = { onStatusFilterChange(BookingStatus.PENDING) }
                    )
                }

                item {
                    FilterChipPill(
                        label = "Cancelled",
                        accentColor = BrandRed,
                        isSelected = selectedStatus == BookingStatus.CANCELLED,
                        onClick = { onStatusFilterChange(BookingStatus.CANCELLED) }
                    )
                }
            }

            // Results count
            Text(
                text = "${bookings.size} Reservation${if (bookings.size != 1) "s" else ""} Found",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceVariant
            )

            // Bookings List
            if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No reservations match your criteria",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing filters or add a new booking",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            currencySymbol = currencySymbol,
                            onStatusChange = { newStatus -> onStatusChange(booking.id, newStatus) },
                            onEdit = { onEditBooking(booking) },
                            onDelete = { onDeleteBooking(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    accentColor: Color = PrimaryDark,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) PrimaryDark else SurfaceContainerLowest,
        label = "chipBg"
    )
    val textCol by animateColorAsState(
        targetValue = if (isSelected) Color.White else OnSurfaceText,
        label = "chipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (isSelected) PrimaryDark else CardBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (accentColor != PrimaryDark) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Text(
                text = label,
                color = textCol,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
