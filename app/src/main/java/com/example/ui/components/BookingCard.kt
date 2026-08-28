package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenBg
import com.example.ui.theme.BrandGreenBorder
import com.example.ui.theme.BrandMustard
import com.example.ui.theme.BrandMustardBg
import com.example.ui.theme.BrandMustardBorder
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandRedBg
import com.example.ui.theme.BrandRedBorder
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BookingCard(
    booking: Booking,
    currencySymbol: String = "₹",
    onStatusChange: (BookingStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
            maximumFractionDigits = 0
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Booking ID & Status Pill & More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.id,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusBadge(status = booking.status)

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp).testTag("booking_menu_${booking.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Booking") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Booking", color = BrandRed) },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), thickness = 1.dp)

            // Guest Name & Room
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = booking.guestName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceText
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = "Room",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = booking.roomDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Total Amount container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${formatter.format(booking.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceText
                    )
                }
            }

            // Check-in / Check-out Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHECK-IN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = booking.checkIn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceText
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHECK-OUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = booking.checkOut,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceText
                    )
                }
            }

            // Contact Row with dial intent
            if (booking.contactNumber.isNotBlank()) {
                HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "CONTACT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp,
                        color = OnSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.contactNumber.replace(" ", "")}"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Phone",
                            tint = OnSurfaceText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = booking.contactNumber,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = OnSurfaceText
                        )
                    }
                }
            }

            // Action Buttons: Quick Status Switchers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusActionButton(
                    title = "Pending",
                    isSelected = booking.status == BookingStatus.PENDING,
                    activeColor = BrandMustard,
                    bgColor = BrandMustardBg,
                    borderColor = BrandMustardBorder,
                    onClick = { onStatusChange(BookingStatus.PENDING) },
                    modifier = Modifier.weight(1f)
                )

                StatusActionButton(
                    title = "Confirmed",
                    isSelected = booking.status == BookingStatus.CONFIRMED,
                    activeColor = BrandGreen,
                    bgColor = BrandGreenBg,
                    borderColor = BrandGreenBorder,
                    onClick = { onStatusChange(BookingStatus.CONFIRMED) },
                    modifier = Modifier.weight(1f)
                )

                StatusActionButton(
                    title = "Cancelled",
                    isSelected = booking.status == BookingStatus.CANCELLED,
                    activeColor = BrandRed,
                    bgColor = BrandRedBg,
                    borderColor = BrandRedBorder,
                    onClick = { onStatusChange(BookingStatus.CANCELLED) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: BookingStatus) {
    val (color, bgColor) = when (status) {
        BookingStatus.CONFIRMED -> BrandGreen to BrandGreenBg
        BookingStatus.PENDING -> BrandMustard to BrandMustardBg
        BookingStatus.CANCELLED -> BrandRed to BrandRedBg
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = status.label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.05.sp
            )
        }
    }
}

@Composable
private fun StatusActionButton(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (isSelected) activeColor else bgColor,
        label = "btnBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else activeColor,
        label = "btnText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("status_btn_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.05.sp
        )
    }
}
