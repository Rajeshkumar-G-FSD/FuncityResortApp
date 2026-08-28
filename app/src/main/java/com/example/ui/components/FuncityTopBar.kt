package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.OnBrandGold
import com.example.ui.theme.PrimaryDark
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FuncityTopBar(
    isSyncing: Boolean,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("11:29:39 pm") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("h:mm:ss a", Locale.ENGLISH)
        while (true) {
            currentTimeString = sdf.format(Date()).lowercase()
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val spinTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing)
        ),
        label = "spinAngle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PrimaryDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Monogram / Logo & Resort Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = OnBrandGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column {
                    Text(
                        text = "Funcity Resorts",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ooty, Nilgiris",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }
            }

            // Right: Actions & Manager Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(36.dp).testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = if (isSyncing) Modifier.rotate(spinAngle) else Modifier
                    )
                }

                IconButton(
                    onClick = { /* Notification Sheet / Info */ },
                    modifier = Modifier.size(36.dp).testTag("notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Profile Avatar Image
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCPbHLt7pjXL1Xz-l3oHmaJmjh5yI6heilu6GW3AQrvDAQdapUeFPiyDom2S34wsDdfDQM91LIZypGlcwLoL4nnMOiVl64wP52058ZIqsLEtcqdfFwSapwlFCyDxVdhmXXfCOH64YTIHCj3kkAwIPOuudkeylmRDcgFahMufUouAyIJelq2mhsqgpMDZ2ZuZgFPpzboP3QDng7AmLYgLtXUz0xGQzzy8FnSoXyybEJT5GRsh15H9Wq5",
                    contentDescription = "Manager Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { onSignOut() }
                )
            }
        }

        // Live Status Sub-header Pill
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF2C2B2B))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(BrandGreen.copy(alpha = pulseAlpha))
                )
                Text(
                    text = "Live · $currentTimeString",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
