package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.F1ViewModel
import com.example.ui.components.F1Card
import com.example.ui.components.TeamIndicator
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailScreen(
    driverId: String,
    viewModel: F1ViewModel,
    onBack: () -> Unit
) {
    val drivers by viewModel.drivers.collectAsState()
    val driver = remember(drivers, driverId) { drivers.find { it.id == driverId } }
    val isFav by viewModel.isFavorite("driver", driverId).collectAsState(initial = false)

    if (driver == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = F1Red)
        }
        return
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DRIVER PROFILE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite("driver", driverId) }) {
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
            // Driver Card Header with full bleed background
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${driver.number}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = teamColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = driver.firstName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = driver.lastName.uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TeamIndicator(colorHex = teamColorHex, modifier = Modifier.height(16.dp).width(3.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = driver.teamName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Faint Driver Code graphic
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = driver.code,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Stats grid
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CAREER STATISTICS",
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
                    StatBox(value = driver.points.toString(), label = "SEASON PTS", modifier = Modifier.weight(1f))
                    StatBox(value = "P${driver.position}", label = "STANDINGS POS", modifier = Modifier.weight(1f), highlightColor = F1YellowSec)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(value = driver.wins.toString(), label = "CAREER WINS", modifier = Modifier.weight(1f))
                    StatBox(value = driver.podiums.toString(), label = "PODIUMS", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(value = driver.careerPoints.toString(), label = "CAREER PTS", modifier = Modifier.weight(1f))
                    StatBox(value = driver.championships.toString(), label = "CHAMPIONSHIPS", modifier = Modifier.weight(1f), highlightColor = F1Red)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Biography
                Text(
                    text = "BIOGRAPHY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = F1Red,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                F1Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NATIONALITY: ${driver.nationality.uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = driver.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlightColor: Color? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = highlightColor ?: MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = F1LightGrey,
                letterSpacing = 0.5.sp
            )
        }
    }
}
