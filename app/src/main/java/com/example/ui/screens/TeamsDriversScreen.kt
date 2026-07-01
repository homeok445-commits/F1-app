package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.DriverEntity
import com.example.data.entities.TeamEntity
import com.example.ui.F1ViewModel
import com.example.ui.components.TeamIndicator
import com.example.ui.theme.*

@Composable
fun TeamsDriversScreen(
    viewModel: F1ViewModel,
    onNavigateToDriverDetail: (String) -> Unit,
    onNavigateToTeamDetail: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("DRIVERS GRID", "TEAMS & BASES")

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
            0 -> DriversGridTab(viewModel = viewModel, onNavigateToDriverDetail = onNavigateToDriverDetail)
            1 -> TeamsGridTab(viewModel = viewModel, onNavigateToTeamDetail = onNavigateToTeamDetail)
        }
    }
}

@Composable
fun DriversGridTab(viewModel: F1ViewModel, onNavigateToDriverDetail: (String) -> Unit) {
    val drivers by viewModel.filteredDrivers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search drivers
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search Driver Name, Code or Country...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = F1Red) },
            modifier = Modifier.fillMaxWidth().testTag("driver_search_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = F1Red,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (drivers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No drivers found matching \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(drivers) { driver ->
                    DriverGridCard(
                        driver = driver,
                        viewModel = viewModel,
                        onClick = { onNavigateToDriverDetail(driver.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DriverGridCard(driver: DriverEntity, viewModel: F1ViewModel, onClick: () -> Unit) {
    val isFav by viewModel.isFavorite("driver", driver.id).collectAsState(initial = false)
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
    val teamColor = remember(teamColorHex) { Color(android.graphics.Color.parseColor(teamColorHex)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("driver_card_${driver.id}")
    ) {
        // Subtle background number representation
        Text(
            text = driver.number.toString(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 10.dp)
        )

        // Favorite Star
        IconButton(
            onClick = { viewModel.toggleFavorite("driver", driver.id) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp)
                .testTag("driver_fav_${driver.id}")
        ) {
            Icon(
                imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite driver",
                tint = if (isFav) F1YellowSec else F1LightGrey,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Team indicator stripe
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(teamColor)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Racing number badge
            Text(
                text = "#${driver.number}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = teamColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Name
            Text(
                text = driver.firstName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = driver.lastName.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code and Team
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = driver.code,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = driver.nationality,
                    fontSize = 11.sp,
                    color = F1LightGrey
                )
            }
        }
    }
}

@Composable
fun TeamsGridTab(viewModel: F1ViewModel, onNavigateToTeamDetail: (String) -> Unit) {
    val teams by viewModel.filteredTeams.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search teams
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search Team, Base or Engine...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = F1Red) },
            modifier = Modifier.fillMaxWidth().testTag("team_search_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = F1Red,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (teams.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No teams found matching \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(teams) { team ->
                    TeamRowCard(
                        team = team,
                        viewModel = viewModel,
                        onClick = { onNavigateToTeamDetail(team.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TeamRowCard(team: TeamEntity, viewModel: F1ViewModel, onClick: () -> Unit) {
    val isFav by viewModel.isFavorite("team", team.id).collectAsState(initial = false)
    val teamColor = remember(team.colorHex) { Color(android.graphics.Color.parseColor(team.colorHex)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("team_card_${team.id}")
    ) {
        // Favorite Star
        IconButton(
            onClick = { viewModel.toggleFavorite("team", team.id) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .testTag("team_fav_${team.id}")
        ) {
            Icon(
                imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite team",
                tint = if (isFav) F1YellowSec else F1LightGrey,
                modifier = Modifier.size(18.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamIndicator(colorHex = team.colorHex, modifier = Modifier.height(60.dp).width(4.dp))
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Base: ${team.base}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Principal: ${team.principal}",
                    fontSize = 11.sp,
                    color = F1LightGrey
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text("POWER UNIT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = F1LightGrey)
                        Text(team.powerUnit, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = teamColor)
                    }
                    Column {
                        Text("CHAMPIONSHIPS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = F1LightGrey)
                        Text(team.championships.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column {
                        Text("GRID POS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = F1LightGrey)
                        Text("P${team.position}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = F1YellowSec)
                    }
                }
            }
        }
    }
}
