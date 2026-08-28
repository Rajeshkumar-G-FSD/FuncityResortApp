package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.FirebaseManager
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnBrandGold
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.WarmBackground

@Composable
fun SettingsScreen(
    currencySymbol: String,
    syncMessage: String?,
    isSyncing: Boolean,
    onCurrencyChange: (String) -> Unit,
    onSeedData: () -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Resort Settings & Cloud",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceText
            )
        }

        // Manager Profile Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                color = SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCPbHLt7pjXL1Xz-l3oHmaJmjh5yI6heilu6GW3AQrvDAQdapUeFPiyDom2S34wsDdfDQM91LIZypGlcwLoL4nnMOiVl64wP52058ZIqsLEtcqdfFwSapwlFCyDxVdhmXXfCOH64YTIHCj3kkAwIPOuudkeylmRDcgFahMufUouAyIJelq2mhsqgpMDZ2ZuZgFPpzboP3QDng7AmLYgLtXUz0xGQzzy8FnSoXyybEJT5GRsh15H9Wq5",
                        contentDescription = "Manager Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, BrandGold, CircleShape)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Resort Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceText
                        )
                        Text(
                            text = "admin@funcity.com",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "Funcity Resorts · Ooty, Nilgiris",
                            fontSize = 11.sp,
                            color = BrandGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Firebase Cloud Firestore Status Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "FIREBASE FIRESTORE SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.05.sp,
                                color = OnSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Active", fontSize = 10.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                    ConfigRow(label = "Project ID", value = FirebaseManager.PROJECT_ID)
                    ConfigRow(label = "Storage Bucket", value = FirebaseManager.STORAGE_BUCKET)
                    ConfigRow(label = "Collection", value = "bookings")
                    ConfigRow(label = "Status", value = syncMessage ?: "Synced")

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryDark,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).testTag("settings_sync_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Sync Now", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onSeedData,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).testTag("settings_seed_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Seed Data", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Currency Selector
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
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
                    Text(
                        text = "CURRENCY DISPLAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("₹" to "INR (₹)", "$" to "USD ($)", "€" to "EUR (€)", "£" to "GBP (£)").forEach { (symbol, name) ->
                            val isSelected = currencySymbol == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandGold else Color.Transparent)
                                    .border(1.dp, if (isSelected) BrandGold else CardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onCurrencyChange(symbol) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = symbol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OnBrandGold else OnSurfaceText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sign Out Button
        item {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandRed.copy(alpha = 0.1f),
                    contentColor = BrandRed
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant)
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceText
        )
    }
}
