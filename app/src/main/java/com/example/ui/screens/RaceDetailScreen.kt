package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.RaceEntity
import com.example.ui.F1ViewModel
import com.example.ui.components.F1Card
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceDetailScreen(
    round: Int,
    viewModel: F1ViewModel,
    onBack: () -> Unit
) {
    val races by viewModel.races.collectAsState()
    val race = remember(races, round) { races.find { it.round == round } }

    if (race == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = F1Red)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CIRCUIT & DETAILS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                .padding(16.dp)
                .padding(bottom = 30.dp)
        ) {
            // Header
            Text(
                text = "ROUND ${race.round}",
                color = F1Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = race.name.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${race.city}, ${race.country}".uppercase(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Circuit Map Vector representation (Red Bull Ring schematic)
            Text(
                text = "CIRCUIT LAYOUT (DRS ZONES & CORNERS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = F1Red,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            F1Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(F1Black, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw a vector representation of Spielberg's Red Bull Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            // Austrian GP Red Bull Ring rough trace layout
                            moveTo(size.width * 0.15f, size.height * 0.85f) // Turn 1 entry
                            lineTo(size.width * 0.45f, size.height * 0.75f) // Turn 1 exit / straight
                            lineTo(size.width * 0.85f, size.height * 0.15f) // Turn 3 hairpin entry
                            quadraticTo(size.width * 0.92f, size.height * 0.12f, size.width * 0.85f, size.height * 0.25f) // Turn 3 apex
                            lineTo(size.width * 0.70f, size.height * 0.50f) // Turn 4 downhill straight
                            quadraticTo(size.width * 0.65f, size.height * 0.55f, size.width * 0.58f, size.height * 0.50f) // Turn 4/5 apex
                            lineTo(size.width * 0.45f, size.height * 0.42f) // Turn 6 downhill
                            quadraticTo(size.width * 0.35f, size.height * 0.45f, size.width * 0.30f, size.height * 0.55f) // Turn 7 entry
                            lineTo(size.width * 0.22f, size.height * 0.68f) // Turn 8/9 entry
                            quadraticTo(size.width * 0.15f, size.height * 0.78f, size.width * 0.15f, size.height * 0.85f) // Close loop to turn 10
                        }

                        // Draw track asphalt lines
                        drawPath(
                            path = path,
                            color = F1Red,
                            style = Stroke(width = 8f)
                        )
                        drawPath(
                            path = path,
                            color = F1White,
                            style = Stroke(width = 2f)
                        )
                    }
                    
                    // Circuit labels overlay
                    Text(
                        text = "START/FINISH",
                        color = F1White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 10.dp, y = (-20).dp)
                    )
                    Text(
                        text = "DRS ZONE 1",
                        color = F1GreenSec,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-30).dp, y = 30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Circuit Specs
            Text(
                text = "CIRCUIT SPECIFICATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = F1Red,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            F1Card(modifier = Modifier.fillMaxWidth()) {
                CircuitSpecRow(label = "Circuit Name", value = race.circuitName)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                CircuitSpecRow(label = "Lap Distance", value = race.circuitLength.ifEmpty { "4.318 km" })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                CircuitSpecRow(label = "Number of Laps", value = if (race.lapCount > 0) race.lapCount.toString() else "71")
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                CircuitSpecRow(label = "Lap Record", value = race.recordTime.ifEmpty { "1:05.619" })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                CircuitSpecRow(label = "Record Holder", value = race.recordHolder.ifEmpty { "Carlos Sainz (2020)" })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weekend Schedule
            Text(
                text = "RACE WEEKEND TIMETABLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = F1Red,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            F1Card(modifier = Modifier.fillMaxWidth()) {
                ScheduleRow(session = "Practice 1", time = race.practice1Time.ifEmpty { "Friday - 11:30" })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                ScheduleRow(session = "Practice 2", time = race.practice2Time.ifEmpty { "Friday - 15:00" })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                ScheduleRow(session = "Practice 3", time = race.practice3Time.ifEmpty { "Saturday - 10:30" })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                ScheduleRow(session = "Qualifying Session", time = race.qualifyingTime.ifEmpty { "Saturday - 14:00" })
                if (race.sprintTime.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                    ScheduleRow(session = "Sprint Race", time = race.sprintTime)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                ScheduleRow(session = "Grand Prix Race", time = "Sunday - ${race.timeStr}")
            }
        }
    }
}

@Composable
fun CircuitSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = F1Red,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun ScheduleRow(session: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = F1LightGrey,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = session,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = time,
            fontSize = 12.sp,
            color = if (time == "Completed") F1GreenSec else F1Red,
            fontWeight = FontWeight.Bold
        )
    }
}
