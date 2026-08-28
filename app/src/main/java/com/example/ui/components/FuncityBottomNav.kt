package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.BrandGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OnBrandGold
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.WarmSurface
import com.example.ui.viewmodel.AppNavTab

@Composable
fun FuncityBottomNav(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = WarmSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                tab = AppNavTab.DASHBOARD,
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = currentTab == AppNavTab.DASHBOARD,
                onClick = { onTabSelected(AppNavTab.DASHBOARD) }
            )
            NavItem(
                tab = AppNavTab.ROOM_CALENDAR,
                icon = Icons.Default.CalendarMonth,
                label = "Calendar",
                isSelected = currentTab == AppNavTab.ROOM_CALENDAR,
                onClick = { onTabSelected(AppNavTab.ROOM_CALENDAR) }
            )
            NavItem(
                tab = AppNavTab.BOOKINGS,
                icon = Icons.Default.FormatListBulleted,
                label = "Bookings",
                isSelected = currentTab == AppNavTab.BOOKINGS,
                onClick = { onTabSelected(AppNavTab.BOOKINGS) }
            )
            NavItem(
                tab = AppNavTab.ANALYTICS,
                icon = Icons.Default.Analytics,
                label = "Analytics",
                isSelected = currentTab == AppNavTab.ANALYTICS,
                onClick = { onTabSelected(AppNavTab.ANALYTICS) }
            )
            NavItem(
                tab = AppNavTab.SETTINGS,
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentTab == AppNavTab.SETTINGS,
                onClick = { onTabSelected(AppNavTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: AppNavTab,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) BrandGold else Color.Transparent,
        label = "navBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) OnBrandGold else OnSurfaceVariant,
        label = "navColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("nav_tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.05.sp
            )
        }
    }
}

