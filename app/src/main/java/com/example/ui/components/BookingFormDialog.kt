package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.ui.theme.BrandGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnBrandGold
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.WarmSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormDialog(
    initialBooking: Booking?,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (Booking) -> Unit
) {
    val isEditing = initialBooking != null

    var guestName by remember { mutableStateOf(initialBooking?.guestName ?: "") }
    var contactNumber by remember { mutableStateOf(initialBooking?.contactNumber ?: "") }
    var guestEmail by remember { mutableStateOf(initialBooking?.guestEmail ?: "") }
    var roomType by remember { mutableStateOf(initialBooking?.roomType ?: "Couple Room") }
    var roomNumber by remember { mutableStateOf(initialBooking?.roomNumber ?: "Room 111") }
    var totalAmount by remember { mutableStateOf(initialBooking?.totalAmount?.toInt()?.toString() ?: "4480") }
    var advanceCollected by remember { mutableStateOf(initialBooking?.advanceCollected?.toInt()?.toString() ?: "967") }
    var checkIn by remember { mutableStateOf(initialBooking?.checkIn ?: "24 Oct, 2:00 PM") }
    var checkOut by remember { mutableStateOf(initialBooking?.checkOut ?: "26 Oct, 11:00 AM") }
    var status by remember { mutableStateOf(initialBooking?.status ?: BookingStatus.CONFIRMED) }
    var notes by remember { mutableStateOf(initialBooking?.notes ?: "") }

    var roomTypeExpanded by remember { mutableStateOf(false) }
    val roomTypes = listOf(
        "Couple Room",
        "Nilgiri Mountain Villa",
        "Heritage Suite",
        "Pine Cottage",
        "Family Garden Haven",
        "Royal Presidential Suite"
    )

    var nameError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = WarmSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Booking" else "New Reservation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceText
                        )
                        if (isEditing) {
                            Text(
                                text = "Booking ID: ${initialBooking?.id}",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Guest Name
                OutlinedTextField(
                    value = guestName,
                    onValueChange = {
                        guestName = it
                        nameError = false
                    },
                    label = { Text("Guest Full Name *") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Guest name is required") } } else null,
                    modifier = Modifier.fillMaxWidth().testTag("guest_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        unfocusedBorderColor = CardBorder
                    )
                )

                // Contact & Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f).testTag("phone_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    OutlinedTextField(
                        value = guestEmail,
                        onValueChange = { guestEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.weight(1f).testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }

                // Room Type Selector
                ExposedDropdownMenuBox(
                    expanded = roomTypeExpanded,
                    onExpandedChange = { roomTypeExpanded = !roomTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = roomTypeExpanded,
                        onDismissRequest = { roomTypeExpanded = false }
                    ) {
                        roomTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    roomType = type
                                    roomTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Room Number & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room / Villa #") },
                        modifier = Modifier.weight(1f).testTag("room_num_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(BookingStatus.CONFIRMED, BookingStatus.PENDING, BookingStatus.CANCELLED).forEach { st ->
                                val isSelected = status == st
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryDark else Color.Transparent)
                                        .clickable { status = st }
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = st.label.take(4),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Financials: Total Amount & Advance Collected
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it },
                        label = { Text("Total Tariff ($currencySymbol)") },
                        modifier = Modifier.weight(1f).testTag("total_amount_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    OutlinedTextField(
                        value = advanceCollected,
                        onValueChange = { advanceCollected = it },
                        label = { Text("Advance Paid ($currencySymbol)") },
                        modifier = Modifier.weight(1f).testTag("advance_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }

                // Check-In & Check-Out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = checkIn,
                        onValueChange = { checkIn = it },
                        label = { Text("Check-In Date/Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    OutlinedTextField(
                        value = checkOut,
                        onValueChange = { checkOut = it },
                        label = { Text("Check-Out Date/Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDark,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }

                // Notes / Requests
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Requests / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        unfocusedBorderColor = CardBorder
                    )
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = OnSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.size(12.dp))

                    Button(
                        onClick = {
                            if (guestName.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val newBooking = Booking(
                                id = initialBooking?.id ?: ("FC-" + (100000..999999).random().toString()),
                                guestName = guestName.trim(),
                                roomType = roomType,
                                roomNumber = roomNumber.ifBlank { "Room 101" },
                                totalAmount = totalAmount.toDoubleOrNull() ?: 0.0,
                                advanceCollected = advanceCollected.toDoubleOrNull() ?: 0.0,
                                checkIn = checkIn.ifBlank { "Today, 2:00 PM" },
                                checkOut = checkOut.ifBlank { "Tomorrow, 11:00 AM" },
                                contactNumber = contactNumber,
                                status = status,
                                guestEmail = guestEmail,
                                notes = notes,
                                createdAt = initialBooking?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(newBooking)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGold,
                            contentColor = OnBrandGold
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("save_booking_button")
                    ) {
                        Text(
                            text = if (isEditing) "Save Changes" else "Create Booking",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
