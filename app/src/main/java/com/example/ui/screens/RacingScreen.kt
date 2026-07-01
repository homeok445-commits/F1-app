package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import com.example.data.entities.LiveTimingEntity
import com.example.data.entities.RaceEntity
import com.example.ui.F1ViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun RacingScreen(
    viewModel: F1ViewModel,
    onNavigateToRaceDetail: (RaceEntity) -> Unit,
    onNavigateToDriverDetail: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("2026 CALENDAR", "LIVE TIMING BOARD")

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = F1Red,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = F1Red
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("racing_tab_$index")
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 14.dp),
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selectedTab == index) F1Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> CalendarTab(viewModel = viewModel, onNavigateToRaceDetail = onNavigateToRaceDetail)
            1 -> LiveTimingTab(viewModel = viewModel, onNavigateToDriverDetail = onNavigateToDriverDetail)
        }
    }
}

@Composable
fun CalendarTab(viewModel: F1ViewModel, onNavigateToRaceDetail: (RaceEntity) -> Unit) {
    val races by viewModel.filteredRaces.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search GP, Circuit or Country...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = F1Red) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = F1LightGrey)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("circuit_search_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = F1Red,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (races.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No races found matching \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp) // Avoid overlap with bottom bar
            ) {
                items(races) { race ->
                    CalendarCard(race = race, onClick = { onNavigateToRaceDetail(race) })
                }
            }
        }
    }
}

@Composable
fun CalendarCard(race: RaceEntity, onClick: () -> Unit) {
    F1Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        hasRedBorder = race.status == "live"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (race.status == "completed") F1MediumGrey else F1Red.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ROUND ${race.round}",
                            color = if (race.status == "completed") F1LightGrey else F1Red,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    if (race.status == "completed") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(F1GreenSec.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FINISHED",
                                color = F1GreenSec,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (race.status == "live") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(F1Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = F1White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = race.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${race.city}, ${race.country}".uppercase(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = race.dateStr.split(",").firstOrNull() ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = race.timeStr,
                    fontSize = 11.sp,
                    color = F1LightGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (race.status == "completed" && race.winnerName != null) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Winner",
                        tint = F1YellowSec,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Winner: ${race.winnerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " (${race.winnerTeam})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = race.winnerTime ?: "",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = F1LightGrey
                )
            }
        }
    }
}

@Composable
fun LiveTimingTab(viewModel: F1ViewModel, onNavigateToDriverDetail: (String) -> Unit) {
    val liveTimingList by viewModel.liveTiming.collectAsState()
    val isLiveTimingActive by viewModel.isLiveTimingActive.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        // Timing Header Action
        F1Card(hasRedBorder = isLiveTimingActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUSTRIAN GP - PRACTICE 3",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = F1Red,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isLiveTimingActive) "SIMULATION ACTIVE" else "SIMULATION PAUSED",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Updates position & laptimes every 4 seconds",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                F1Button(
                    text = if (isLiveTimingActive) "PAUSE" else "START",
                    onClick = { viewModel.toggleLiveTiming() },
                    tag = "timing_action_btn"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Timing Table Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("POS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(30.dp))
            Text("DRIVER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
            Text("GAP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("LAST LAP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
            Text("S1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            Text("S2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            Text("S3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Timing Rows List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(liveTimingList, key = { it.driverId }) { row ->
                TimingTableRow(row = row, onClick = { onNavigateToDriverDetail(row.driverId) })
            }
        }
    }
}

@Composable
fun TimingTableRow(row: LiveTimingEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = row.position.toString(),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = if (row.position == 1) F1YellowSec else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(30.dp)
        )

        // Team Stripe & Driver
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(60.dp)
        ) {
            TeamIndicator(colorHex = row.teamColorHex, modifier = Modifier.height(14.dp).width(3.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = row.driverCode,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Gap to leader
        Text(
            text = row.gapToLeader,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        // Last Lap Time
        Text(
            text = row.lastLapTime,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (row.isFastestLap) F1PurpleSec else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(75.dp),
            textAlign = TextAlign.End
        )

        // Sectors (colored sector green if fast)
        Text(
            text = row.sector1,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (row.isFastestLap && Random.nextBoolean()) F1PurpleSec else F1GreenSec,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = row.sector2,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (row.isFastestLap && Random.nextBoolean()) F1PurpleSec else F1GreenSec,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = row.sector3,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (row.isFastestLap && Random.nextBoolean()) F1PurpleSec else F1GreenSec,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}
