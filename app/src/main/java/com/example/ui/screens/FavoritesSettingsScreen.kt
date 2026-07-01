package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun FavoritesSettingsScreen(
    viewModel: F1ViewModel,
    onNavigateToDriverDetail: (String) -> Unit,
    onNavigateToTeamDetail: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val favoritesList by viewModel.favorites.collectAsState()
    val allDrivers by viewModel.drivers.collectAsState()
    val allTeams by viewModel.teams.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()

    // Resolve favorite drivers and teams objects
    val favoriteDrivers = remember(favoritesList, allDrivers) {
        val favDriverIds = favoritesList.filter { it.type == "driver" }.map { it.itemId }
        allDrivers.filter { it.id in favDriverIds }
    }

    val favoriteTeams = remember(favoritesList, allTeams) {
        val favTeamIds = favoritesList.filter { it.type == "team" }.map { it.itemId }
        allTeams.filter { it.id in favTeamIds }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 90.dp)
    ) {
        // Favorites Section
        Text(
            text = "YOUR FAVORITES",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (favoriteDrivers.isEmpty() && favoriteTeams.isEmpty()) {
            F1Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = "Star",
                        tint = F1Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Favorites pinned",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Star your favorite drivers or teams to quickly pin them here on your dashboard.",
                        color = F1LightGrey,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            // Horizontal list or rows of favorite drivers
            if (favoriteDrivers.isNotEmpty()) {
                Text(
                    text = "FAVORITE DRIVERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = F1Red,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                favoriteDrivers.forEach { driver ->
                    DriverStandingRow(driver = driver, onClick = { onNavigateToDriverDetail(driver.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Favorite teams
            if (favoriteTeams.isNotEmpty()) {
                Text(
                    text = "FAVORITE TEAMS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = F1Red,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                favoriteTeams.forEach { team ->
                    ConstructorStandingRow(team = team, onClick = { onNavigateToTeamDetail(team.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notifications Panel
        Text(
            text = "PUSH NOTIFICATION ALERTS",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        F1Card {
            NotificationOption(
                title = "Race Day Reminders",
                description = "Get notified 1 hour before Grand Prix start times.",
                isChecked = notificationSettings.find { it.eventType == "race_reminder" }?.isEnabled ?: true,
                onCheckedChange = { viewModel.toggleNotification("race_reminder", it) },
                tag = "notif_race"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            NotificationOption(
                title = "Breaking News Alerts",
                description = "Immediate push notifications for crucial paddock announcements.",
                isChecked = notificationSettings.find { it.eventType == "breaking_news" }?.isEnabled ?: true,
                onCheckedChange = { viewModel.toggleNotification("breaking_news", it) },
                tag = "notif_news"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            NotificationOption(
                title = "Session Start Alarms",
                description = "Trigger notification reminders for Practice and Qualifying.",
                isChecked = notificationSettings.find { it.eventType == "session_start" }?.isEnabled ?: false,
                onCheckedChange = { viewModel.toggleNotification("session_start", it) },
                tag = "notif_session"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Styling & Mode preferences
        Text(
            text = "THEME PREFERENCES",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        F1Card {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Premium Dark Mode",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toggle between F1 Dark Carbon and Light styling.",
                        color = F1LightGrey,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onToggleTheme,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = F1Red,
                        checkedTrackColor = F1Red.copy(alpha = 0.4f),
                        uncheckedThumbColor = F1LightGrey,
                        uncheckedTrackColor = F1MediumGrey
                    ),
                    modifier = Modifier.testTag("theme_toggle_switch")
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Version info
        Text(
            text = "F1 ULTIMATE v1.0.0 (OFFLINE SUPPORT ENABLED)",
            fontSize = 9.sp,
            color = F1LightGrey,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NotificationOption(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                color = F1LightGrey,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = F1Red,
                checkedTrackColor = F1Red.copy(alpha = 0.4f),
                uncheckedThumbColor = F1LightGrey,
                uncheckedTrackColor = F1MediumGrey
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
