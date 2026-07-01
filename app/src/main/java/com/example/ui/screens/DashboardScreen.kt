package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.entities.LiveTimingEntity
import com.example.data.entities.NewsEntity
import com.example.ui.F1ViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: F1ViewModel,
    onNavigateToTiming: () -> Unit,
    onNavigateToNewsDetail: (NewsEntity) -> Unit,
    onNavigateToDriverDetail: (String) -> Unit
) {
    val newsList by viewModel.news.collectAsState()
    val liveTimingList by viewModel.liveTiming.collectAsState()
    val isLiveTimingActive by viewModel.isLiveTimingActive.collectAsState()
    val isRefreshingNews by viewModel.isRefreshingNews.collectAsState()

    // 2026 Austrian GP target: July 5th, 2026, 13:00 UTC
    val austrianGpTimeMs = 1783314000000L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // Avoid overlap with bottom nav
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(F1Red.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "F1 ULTIMATE",
                        color = F1Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Welcome, F1 Fan!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Status banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLiveTimingActive) F1GreenSec.copy(alpha = 0.15f) else F1Red.copy(alpha = 0.1f))
                        .border(
                            1.dp,
                            if (isLiveTimingActive) F1GreenSec else F1Red.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLiveTimingActive) F1GreenSec.copy(alpha = alpha) 
                                    else F1Red.copy(alpha = alpha)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveTimingActive) "SIM LIVE" else "READY",
                            color = if (isLiveTimingActive) F1GreenSec else F1Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Countdown Widget
            NextRaceCountdown(
                raceName = "Austrian Grand Prix 2026",
                circuitName = "Red Bull Ring, Spielberg",
                targetTimestampMs = austrianGpTimeMs
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Live Timing Widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE TIMING SIMULATOR",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isLiveTimingActive) "STOP SIM" else "START SIM",
                    color = F1Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.toggleLiveTiming() }
                        .testTag("timing_toggle_btn")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            F1Card(
                onClick = onNavigateToTiming,
                hasRedBorder = isLiveTimingActive
            ) {
                if (liveTimingList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No timing data available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Top 3 Leaderboard Preview
                            liveTimingList.take(3).forEach { timing ->
                                MiniLeaderboardRow(timing = timing, onNavigateToDriverDetail = onNavigateToDriverDetail)
                                if (timing.position < 3) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        // Action chevron
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open detailed timing",
                            tint = F1Red,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Latest News Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LATEST FORMULA 1 NEWS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(F1Red)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI REFRESH",
                            color = F1White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                IconButton(
                    onClick = { viewModel.refreshNews() },
                    modifier = Modifier.size(24.dp).testTag("news_refresh_btn")
                ) {
                    if (isRefreshingNews) {
                        CircularProgressIndicator(color = F1Red, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh news with Gemini",
                            tint = F1Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal News Scroll
            if (newsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No news available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(newsList) { article ->
                        NewsRowCard(article = article, onClick = { onNavigateToNewsDetail(article) })
                    }
                }
            }
        }
    }
}

@Composable
fun MiniLeaderboardRow(timing: LiveTimingEntity, onNavigateToDriverDetail: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToDriverDetail(timing.driverId) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "P${timing.position}",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = if (timing.position == 1) F1YellowSec else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(36.dp)
            )
            
            TeamIndicator(
                colorHex = timing.teamColorHex,
                modifier = Modifier
                    .height(14.dp)
                    .width(3.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timing.driverCode,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (timing.isFastestLap) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(F1PurpleSec.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PURPLE",
                        color = F1PurpleSec,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = timing.lastLapTime,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = if (timing.isFastestLap) F1PurpleSec else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NewsRowCard(article: NewsEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick)
            .testTag("news_card_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            SubcomposeAsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(F1DarkGrey, F1MediumGrey)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "F1 NEWS",
                            color = F1Red,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp
                        )
                    }
                }
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (article.category.lowercase()) {
                                "breaking" -> F1Red.copy(alpha = 0.15f)
                                "tech" -> F1BlueSec.copy(alpha = 0.15f)
                                "interview" -> F1GreenSec.copy(alpha = 0.15f)
                                else -> F1LightGrey.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        color = when (article.category.lowercase()) {
                            "breaking" -> F1Red
                            "tech" -> F1BlueSec
                            "interview" -> F1GreenSec
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = article.dateStr,
                    fontSize = 9.sp,
                    color = F1LightGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
