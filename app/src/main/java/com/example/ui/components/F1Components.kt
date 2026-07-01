package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

// Carbon-style F1 card with red accent options
@Composable
fun F1Card(
    modifier: Modifier = Modifier,
    hasRedBorder: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(
            width = 1.dp,
            color = if (hasRedBorder) F1Red.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        )
    
    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Column(
        modifier = finalModifier.padding(16.dp),
        content = content
    )
}

// Modern racing-style red button
@Composable
fun F1Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    tag: String = "f1_button"
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = F1Red,
            contentColor = F1White,
            disabledContainerColor = F1MediumGrey,
            disabledContentColor = F1LightGrey
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        )
    }
}

// Team color stripe or indicator
@Composable
fun TeamIndicator(
    colorHex: String,
    modifier: Modifier = Modifier,
    width: Dp = 4.dp
) {
    val color = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            F1Red
        }
    }
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

// Dynamic countdown widget for the upcoming Austrian GP (July 5th, 2026, 13:00 UTC)
@Composable
fun NextRaceCountdown(
    raceName: String,
    circuitName: String,
    targetTimestampMs: Long
) {
    var timeLeft by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(targetTimestampMs) {
        while (true) {
            val nowMs = System.currentTimeMillis()
            val diffMs = targetTimestampMs - nowMs
            timeLeft = if (diffMs > 0) Duration.ofMillis(diffMs) else Duration.ZERO
            delay(1000)
        }
    }

    F1Card(
        modifier = Modifier.fillMaxWidth(),
        hasRedBorder = true
    ) {
        Text(
            text = "NEXT RACE COUNTDOWN",
            color = F1Red,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = raceName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = circuitName.uppercase(),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountdownUnit(value = timeLeft.toDays().toInt(), label = "DAYS")
            CountdownSeparator()
            CountdownUnit(value = (timeLeft.toHours() % 24).toInt(), label = "HRS")
            CountdownSeparator()
            CountdownUnit(value = (timeLeft.toMinutes() % 60).toInt(), label = "MIN")
            CountdownSeparator()
            CountdownUnit(value = (timeLeft.getSeconds() % 60).toInt(), label = "SEC")
        }
    }
}

@Composable
private fun CountdownUnit(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format(Locale.US, "%02d", value),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = F1Red,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CountdownSeparator() {
    Text(
        text = ":",
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.offset(y = (-4).dp)
    )
}
