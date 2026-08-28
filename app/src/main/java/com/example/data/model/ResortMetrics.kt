package com.example.data.model

data class DailyRevenuePoint(
    val dayLabel: String,
    val amount: Double
)

data class ResortMetrics(
    val confirmedRevenue: Double = 0.0,
    val advanceCollected: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val pendingCount: Int = 0,
    val cancelledToday: Int = 0,
    val confirmedCount: Int = 0,
    val totalBookings: Int = 0,
    val occupancyRate: Int = 78,
    val revenueHistory: List<DailyRevenuePoint> = emptyList()
) {
    val confirmedPercentage: Float
        get() = if (totalBookings > 0) (confirmedCount.toFloat() / totalBookings.toFloat()) * 100f else 70f

    val pendingPercentage: Float
        get() = if (totalBookings > 0) (pendingCount.toFloat() / totalBookings.toFloat()) * 100f else 20f

    val cancelledPercentage: Float
        get() = if (totalBookings > 0) (cancelledToday.toFloat() / totalBookings.toFloat()) * 100f else 10f
}
