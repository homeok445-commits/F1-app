package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.F1ViewModel
import com.example.ui.components.F1Card
import com.example.ui.components.TeamIndicator
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    viewModel: F1ViewModel,
    onBack: () -> Unit,
    onNavigateToDriverDetail: (String) -> Unit
) {
    val teams by viewModel.teams.collectAsState()
    val team = remember(teams, teamId) { teams.find { it.id == teamId } }
    val isFav by viewModel.isFavorite("team", teamId).collectAsState(initial = false)

    // Filter active drivers belonging to this team
    val allDrivers by viewModel.drivers.collectAsState()
    val teamDrivers = remember(allDrivers, teamId) { allDrivers.filter { it.teamId == teamId } }

    if (team == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = F1Red)
        }
        return
    }

    val teamColor = remember(team.colorHex) { Color(android.graphics.Color.parseColor(team.colorHex)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TEAM PROFILE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite("team", teamId) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) F1YellowSec else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Team Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(teamColor.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamIndicator(colorHex = team.colorHex, modifier = Modifier.height(72.dp).width(4.dp))
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = team.shortName.uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 36.sp
                        )
                        Text(
                            text = team.name,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Team Specs
                Text(
                    text = "TEAM INFORMATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = F1Red,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                F1Card(modifier = Modifier.fillMaxWidth()) {
                    TeamSpecRow(label = "Team Principal", value = team.principal)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                    TeamSpecRow(label = "Headquarters", value = team.base)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                    TeamSpecRow(label = "Power Unit", value = team.powerUnit)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Box
                Text(
                    text = "SEASON STATISTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = F1Red,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(value = team.points.toString(), label = "CONSTRUCTOR POINTS", modifier = Modifier.weight(1f))
                    StatBox(value = "P${team.position}", label = "GRID POSITION", modifier = Modifier.weight(1f), highlightColor = F1YellowSec)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(value = team.championships.toString(), label = "WORLD TITLES", modifier = Modifier.weight(1f), highlightColor = F1Red)
                    StatBox(value = teamDrivers.size.toString(), label = "ACTIVE DRIVERS", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Team Drivers
                if (teamDrivers.isNotEmpty()) {
                    Text(
                        text = "ACTIVE DRIVERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = F1Red,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    teamDrivers.forEach { driver ->
                        DriverStandingRow(driver = driver, onClick = { onNavigateToDriverDetail(driver.id) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TeamSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
