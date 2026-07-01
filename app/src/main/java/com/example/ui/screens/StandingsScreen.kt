package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.DriverEntity
import com.example.data.entities.TeamEntity
import com.example.ui.F1ViewModel
import com.example.ui.components.TeamIndicator
import com.example.ui.theme.*

@Composable
fun StandingsScreen(
    viewModel: F1ViewModel,
    onNavigateToDriverDetail: (String) -> Unit,
    onNavigateToTeamDetail: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("DRIVERS", "CONSTRUCTORS")

    Column(modifier = Modifier.fillMaxSize()) {
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
                    onClick = { selectedTab = index }
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
            0 -> DriverStandingsTab(viewModel = viewModel, onNavigateToDriverDetail = onNavigateToDriverDetail)
            1 -> ConstructorStandingsTab(viewModel = viewModel, onNavigateToTeamDetail = onNavigateToTeamDetail)
        }
    }
}

@Composable
fun DriverStandingsTab(viewModel: F1ViewModel, onNavigateToDriverDetail: (String) -> Unit) {
    val drivers by viewModel.drivers.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        items(drivers) { driver ->
            DriverStandingRow(driver = driver, onClick = { onNavigateToDriverDetail(driver.id) })
        }
    }
}

@Composable
fun DriverStandingRow(driver: DriverEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = driver.position.toString(),
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = when (driver.position) {
                1 -> F1YellowSec
                2 -> F1LightGrey
                3 -> Color(0xFFCD7F32) // Bronze
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.width(32.dp)
        )

        // Team Stripe Indicator
        // Let's lookup a fallback team color or default
        val teamColorHex = when (driver.teamId) {
            "ferrari" -> "#E10600"
            "mclaren" -> "#FF8700"
            "red_bull" -> "#3671C6"
            "mercedes" -> "#27F4D2"
            "aston_martin" -> "#229971"
            "williams" -> "#64C4FF"
            "haas" -> "#B6BABD"
            "rb" -> "#6692FF"
            "alpine" -> "#FF87BC"
            "sauber" -> "#52E252"
            else -> "#FF1801"
        }
        TeamIndicator(colorHex = teamColorHex, modifier = Modifier.height(24.dp).width(3.dp))
        
        Spacer(modifier = Modifier.width(12.dp))

        // Driver details
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = driver.firstName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = driver.lastName.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = driver.teamName,
                fontSize = 11.sp,
                color = F1LightGrey,
                fontWeight = FontWeight.Medium
            )
        }

        // Stats summary
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${driver.points} PTS",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${driver.wins} W / ${driver.podiums} POD",
                fontSize = 10.sp,
                color = F1LightGrey,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ConstructorStandingsTab(viewModel: F1ViewModel, onNavigateToTeamDetail: (String) -> Unit) {
    val teams by viewModel.teams.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        items(teams) { team ->
            ConstructorStandingRow(team = team, onClick = { onNavigateToTeamDetail(team.id) })
        }
    }
}

@Composable
fun ConstructorStandingRow(team: TeamEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = team.position.toString(),
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = when (team.position) {
                1 -> F1YellowSec
                2 -> F1LightGrey
                3 -> Color(0xFFCD7F32) // Bronze
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.width(32.dp)
        )

        TeamIndicator(colorHex = team.colorHex, modifier = Modifier.height(24.dp).width(3.dp))
        
        Spacer(modifier = Modifier.width(12.dp))

        // Team details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = team.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Engine: ${team.powerUnit}",
                fontSize = 11.sp,
                color = F1LightGrey,
                fontWeight = FontWeight.Medium
            )
        }

        // Points
        Text(
            text = "${team.points} PTS",
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
