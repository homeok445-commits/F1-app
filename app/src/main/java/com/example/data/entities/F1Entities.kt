package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val code: String,
    val number: Int,
    val teamId: String,
    val teamName: String,
    val country: String,
    val points: Int,
    val position: Int,
    val wins: Int,
    val podiums: Int,
    val careerPoints: Double,
    val championships: Int,
    val bio: String,
    val nationality: String
) {
    val fullName: String get() = "$firstName $lastName"
}

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val principal: String,
    val base: String,
    val powerUnit: String,
    val championships: Int,
    val points: Int,
    val position: Int,
    val colorHex: String
)

@Entity(tableName = "races")
data class RaceEntity(
    @PrimaryKey val round: Int,
    val name: String,
    val circuitName: String,
    val city: String,
    val country: String,
    val dateStr: String,
    val timeStr: String,
    val timestamp: Long,
    val status: String, // "scheduled", "live", "completed"
    val winnerName: String? = null,
    val winnerTeam: String? = null,
    val winnerTime: String? = null,
    val circuitLength: String = "",
    val lapCount: Int = 0,
    val recordTime: String = "",
    val recordHolder: String = "",
    val practice1Time: String = "",
    val practice2Time: String = "",
    val practice3Time: String = "",
    val qualifyingTime: String = "",
    val sprintTime: String = ""
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val dateStr: String,
    val imageUrl: String,
    val category: String // "breaking", "tech", "interview", "race"
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val compoundId: String, // "driver_verstappen" or "team_red_bull"
    val type: String, // "driver" or "team"
    val itemId: String
)

@Entity(tableName = "notifications")
data class NotificationSettingEntity(
    @PrimaryKey val eventType: String, // "race_reminder", "breaking_news", "session_start"
    val isEnabled: Boolean
)

@Entity(tableName = "live_timing")
data class LiveTimingEntity(
    @PrimaryKey val driverId: String,
    val driverCode: String,
    val position: Int,
    val gapToLeader: String,
    val lastLapTime: String,
    val sector1: String,
    val sector2: String,
    val sector3: String,
    val isFastestLap: Boolean,
    val positionChange: Int, // e.g. +1, -2, 0
    val teamColorHex: String
)
