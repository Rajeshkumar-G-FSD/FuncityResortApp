package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.ResortMetrics
import com.example.ui.components.RevenueLineChart
import com.example.ui.components.StatusSplitDonutChart
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandMustard
import com.example.ui.theme.BrandRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.WarmBackground
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    metrics: ResortMetrics,
    bookings: List<Booking>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
            maximumFractionDigits = 0
        }
    }

    val avgTariff = if (bookings.isNotEmpty()) bookings.map { it.totalAmount }.average() else 4480.0
    val advanceRatio = if (metrics.confirmedRevenue > 0) ((metrics.advanceCollected / metrics.confirmedRevenue) * 100).toInt() else 21

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Performance & Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceText
            )
            Text(
                text = "Funcity Resorts, Ooty - Operational Insights",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        // Summary Metric Highlight
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                color = PrimaryDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "TOTAL GROSS REVENUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$currencySymbol${formatter.format(metrics.confirmedRevenue + metrics.advanceCollected)}",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Advance Ratio", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$advanceRatio%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Average Tariff", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$currencySymbol${formatter.format(avgTariff)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Occupancy", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${metrics.occupancyRate}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                        }
                    }
                }
            }
        }

        // Charts
        item {
            RevenueLineChart(
                history = metrics.revenueHistory,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        item {
            StatusSplitDonutChart(
                metrics = metrics,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        // Room Type Breakdown
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ROOM OCCUPANCY BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp,
                        color = OnSurfaceVariant
                    )

                    RoomProgressItem(
                        name = "Couple Rooms (Room 101 - 115)",
                        occupancy = 85,
                        color = BrandGreen
                    )
                    RoomProgressItem(
                        name = "Nilgiri Mountain Villas (Villa 201 - 208)",
                        occupancy = 70,
                        color = BrandBlue
                    )
                    RoomProgressItem(
                        name = "Heritage Luxury Suites (Suite 301 - 306)",
                        occupancy = 60,
                        color = BrandGold
                    )
                    RoomProgressItem(
                        name = "Pine Cottages (Cottage 101 - 110)",
                        occupancy = 45,
                        color = BrandMustard
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoomProgressItem(
    name: String,
    occupancy: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 13.sp, color = OnSurfaceText, fontWeight = FontWeight.Medium)
            Text(text = "$occupancy%", fontSize = 13.sp, color = OnSurfaceVariant, fontFamily = FontFamily.Monospace)
        }
        LinearProgressIndicator(
            progress = { occupancy / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = CardBorder.copy(alpha = 0.5f)
        )
    }
}
