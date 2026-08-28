package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRevenuePoint
import com.example.data.model.ResortMetrics
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandMustard
import com.example.ui.theme.BrandRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun RevenueLineChart(
    history: List<DailyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "REVENUE LAST 14 DAYS",
                color = OnSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.05.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    if (width <= 0 || height <= 0) return@Canvas

                    val points = if (history.isNotEmpty()) history else listOf(
                        DailyRevenuePoint("D1", 1200.0),
                        DailyRevenuePoint("D3", 1800.0),
                        DailyRevenuePoint("D5", 1400.0),
                        DailyRevenuePoint("D7", 2900.0),
                        DailyRevenuePoint("D9", 2300.0),
                        DailyRevenuePoint("D11", 3800.0),
                        DailyRevenuePoint("D14", 4480.0)
                    )

                    val maxVal = (points.maxOfOrNull { it.amount } ?: 5000.0).coerceAtLeast(1000.0) * 1.15
                    val minVal = 0.0

                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val coordinates = points.mapIndexed { index, pt ->
                        val x = index * stepX
                        val normalizedY = ((pt.amount - minVal) / (maxVal - minVal)).toFloat()
                        val y = height - (normalizedY * (height - 30f)) - 15f
                        Offset(x, y)
                    }

                    // Background Grid Lines
                    for (i in 1..3) {
                        val gridY = (height / 4f) * i
                        drawLine(
                            color = CardBorder.copy(alpha = 0.6f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build Cubic Bezier Path
                    val strokePath = Path().apply {
                        moveTo(coordinates.first().x, coordinates.first().y)
                        for (i in 0 until coordinates.size - 1) {
                            val p0 = coordinates[i]
                            val p1 = coordinates[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                            cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                        }
                    }

                    // Gradient fill path
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BrandBlue.copy(alpha = 0.28f),
                                BrandBlue.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Line Stroke
                    drawPath(
                        path = strokePath,
                        color = BrandBlue,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw key data point circles
                    coordinates.forEachIndexed { i, offset ->
                        if (i == coordinates.lastIndex || i == 3) {
                            drawCircle(
                                color = SurfaceContainerLowest,
                                radius = 6.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = BrandBlue,
                                radius = 4.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }

            // Bottom axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labels = listOf("14d ago", "7d ago", "Today")
                labels.forEach { label ->
                    Text(
                        text = label,
                        color = OnSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun StatusSplitDonutChart(
    metrics: ResortMetrics,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "STATUS SPLIT",
                color = OnSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.05.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Background Track
                    drawArc(
                        color = CardBorder.copy(alpha = 0.5f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )

                    val confirmedAngle = (metrics.confirmedPercentage / 100f) * 360f
                    val pendingAngle = (metrics.pendingPercentage / 100f) * 360f
                    val cancelledAngle = (metrics.cancelledPercentage / 100f) * 360f

                    var currentAngle = -90f

                    // Confirmed Segment (Green)
                    if (confirmedAngle > 0) {
                        drawArc(
                            color = BrandGreen,
                            startAngle = currentAngle,
                            sweepAngle = confirmedAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        currentAngle += confirmedAngle
                    }

                    // Pending Segment (Mustard)
                    if (pendingAngle > 0) {
                        drawArc(
                            color = BrandMustard,
                            startAngle = currentAngle,
                            sweepAngle = pendingAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        currentAngle += pendingAngle
                    }

                    // Cancelled Segment (Red)
                    if (cancelledAngle > 0) {
                        drawArc(
                            color = BrandRed,
                            startAngle = currentAngle,
                            sweepAngle = cancelledAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${metrics.totalBookings}",
                        color = OnSurfaceText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Legend Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = BrandGreen, label = "Conf (${metrics.confirmedCount})")
                LegendDot(color = BrandMustard, label = "Pend (${metrics.pendingCount})")
                LegendDot(color = BrandRed, label = "Canc (${metrics.cancelledToday})")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = OnSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}
