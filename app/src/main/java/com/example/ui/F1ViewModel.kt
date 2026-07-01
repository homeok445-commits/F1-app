package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entities.*
import com.example.data.repository.F1Repository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class F1ViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = F1Repository(application)

    // State Flows from Repository
    val drivers: StateFlow<List<DriverEntity>> = repository.drivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teams: StateFlow<List<TeamEntity>> = repository.teams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val races: StateFlow<List<RaceEntity>> = repository.races
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val news: StateFlow<List<NewsEntity>> = repository.news
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveTiming: StateFlow<List<LiveTimingEntity>> = repository.liveTiming
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationSettings: StateFlow<List<NotificationSettingEntity>> = repository.notificationSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for search and selection
    val searchQuery = MutableStateFlow("")
    val isLiveTimingActive = MutableStateFlow(false)
    val isRefreshingNews = MutableStateFlow(false)

    // Filtered lists based on search query
    val filteredDrivers: StateFlow<List<DriverEntity>> = combine(drivers, searchQuery) { list, query ->
        if (query.isBlank()) list else {
            list.filter {
                it.fullName.contains(query, ignoreCase = true) ||
                it.code.contains(query, ignoreCase = true) ||
                it.teamName.contains(query, ignoreCase = true) ||
                it.country.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTeams: StateFlow<List<TeamEntity>> = combine(teams, searchQuery) { list, query ->
        if (query.isBlank()) list else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.powerUnit.contains(query, ignoreCase = true) ||
                it.base.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRaces: StateFlow<List<RaceEntity>> = combine(races, searchQuery) { list, query ->
        if (query.isBlank()) list else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.circuitName.contains(query, ignoreCase = true) ||
                it.country.contains(query, ignoreCase = true) ||
                it.city.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites support
    fun isFavorite(type: String, id: String): Flow<Boolean> {
        return repository.isFavorite(type, id)
    }

    fun toggleFavorite(type: String, id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(type, id)
        }
    }

    // Notifications toggle
    fun toggleNotification(eventType: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.saveNotificationSetting(eventType, enabled)
        }
    }

    // Refresh News using Gemini
    fun refreshNews() {
        if (isRefreshingNews.value) return
        viewModelScope.launch {
            isRefreshingNews.value = true
            repository.generateF1NewsWithGemini()
            isRefreshingNews.value = false
        }
    }

    // Live timing trigger
    fun toggleLiveTiming() {
        if (isLiveTimingActive.value) {
            repository.stopLiveTimingSimulation()
            isLiveTimingActive.value = false
        } else {
            repository.startLiveTimingSimulation()
            isLiveTimingActive.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopLiveTimingSimulation()
    }
}

class F1ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(F1ViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return F1ViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
