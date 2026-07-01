package com.example.data.dao

import androidx.room.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers ORDER BY position ASC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id")
    fun getDriverById(id: String): Flow<DriverEntity?>

    @Query("SELECT * FROM drivers WHERE teamId = :teamId ORDER BY position ASC")
    fun getDriversByTeam(teamId: String): Flow<List<DriverEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Query("DELETE FROM drivers")
    suspend fun clearDrivers()
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY position ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :id")
    fun getTeamById(id: String): Flow<TeamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("DELETE FROM teams")
    suspend fun clearTeams()
}

@Dao
interface RaceDao {
    @Query("SELECT * FROM races ORDER BY round ASC")
    fun getAllRaces(): Flow<List<RaceEntity>>

    @Query("SELECT * FROM races WHERE round = :round")
    fun getRaceByRound(round: Int): Flow<RaceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaces(races: List<RaceEntity>)

    @Query("UPDATE races SET status = :status WHERE round = :round")
    suspend fun updateRaceStatus(round: Int, status: String)

    @Query("DELETE FROM races")
    suspend fun clearRaces()
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY dateStr DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(newsList: List<NewsEntity>)

    @Query("DELETE FROM news")
    suspend fun clearNews()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE compoundId = :compoundId)")
    fun isFavorite(compoundId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE compoundId = :compoundId")
    suspend fun deleteFavoriteById(compoundId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications")
    fun getSettings(): Flow<List<NotificationSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: NotificationSettingEntity)
}

@Dao
interface LiveTimingDao {
    @Query("SELECT * FROM live_timing ORDER BY position ASC")
    fun getLiveTiming(): Flow<List<LiveTimingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveTiming(timing: List<LiveTimingEntity>)

    @Query("DELETE FROM live_timing")
    suspend fun clearLiveTiming()
}
