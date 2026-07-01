package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.F1InitialData
import com.example.data.database.F1Database
import com.example.data.entities.*
import com.example.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import org.json.JSONArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import kotlin.random.Random

class F1Repository(private val context: Context) {
    private val db = F1Database.getDatabase(context)
    private val driverDao = db.driverDao()
    private val teamDao = db.teamDao()
    private val raceDao = db.raceDao()
    private val newsDao = db.newsDao()
    private val favoriteDao = db.favoriteDao()
    private val notificationDao = db.notificationDao()
    private val liveTimingDao = db.liveTimingDao()

    val drivers: Flow<List<DriverEntity>> = driverDao.getAllDrivers()
    val teams: Flow<List<TeamEntity>> = teamDao.getAllTeams()
    val races: Flow<List<RaceEntity>> = raceDao.getAllRaces()
    val news: Flow<List<NewsEntity>> = newsDao.getAllNews()
    val favorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    val liveTiming: Flow<List<LiveTimingEntity>> = liveTimingDao.getLiveTiming()
    val notificationSettings: Flow<List<NotificationSettingEntity>> = notificationDao.getSettings()

    private var simulationJob: Job? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        repositoryScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        // Seed if teams or drivers are empty
        val existingTeams = teams.firstOrNull() ?: emptyList()
        if (existingTeams.isEmpty()) {
            teamDao.insertTeams(F1InitialData.teams)
            driverDao.insertDrivers(F1InitialData.drivers)
            raceDao.insertRaces(F1InitialData.races)
            newsDao.insertNews(F1InitialData.news)
            liveTimingDao.insertLiveTiming(F1InitialData.liveTimingMock)

            // Seed default notifications
            notificationDao.saveSetting(NotificationSettingEntity("race_reminder", true))
            notificationDao.saveSetting(NotificationSettingEntity("breaking_news", true))
            notificationDao.saveSetting(NotificationSettingEntity("session_start", false))
        }
    }

    fun isFavorite(type: String, id: String): Flow<Boolean> {
        val compoundId = "${type}_$id"
        return favoriteDao.isFavorite(compoundId)
    }

    suspend fun toggleFavorite(type: String, id: String) = withContext(Dispatchers.IO) {
        val compoundId = "${type}_$id"
        val isFav = favoriteDao.isFavorite(compoundId).first()
        if (isFav) {
            favoriteDao.deleteFavoriteById(compoundId)
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(compoundId, type, id))
        }
    }

    suspend fun saveNotificationSetting(eventType: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        notificationDao.saveSetting(NotificationSettingEntity(eventType, enabled))
    }

    // --- Live Timing Simulation ---
    fun startLiveTimingSimulation() {
        if (simulationJob?.isActive == true) return

        simulationJob = repositoryScope.launch {
            var lap = 1
            val currentTiming = F1InitialData.liveTimingMock.toMutableList()
            
            while (isActive) {
                // Shuffle timing slightly
                for (i in currentTiming.indices) {
                    val driver = currentTiming[i]
                    // Simulate random lap sector times
                    val s1 = (18.5 + Random.nextDouble(0.0, 1.2)).roundToDecimal(1)
                    val s2 = (27.5 + Random.nextDouble(0.0, 1.5)).roundToDecimal(1)
                    val s3 = (20.0 + Random.nextDouble(0.0, 1.0)).roundToDecimal(1)
                    
                    val lapTimeMs = (s1 + s2 + s3)
                    val seconds = lapTimeMs.toInt()
                    val millis = ((lapTimeMs - seconds) * 1000).toInt()
                    val lapTimeStr = String.format(Locale.US, "1:%02d.%03d", seconds - 60, millis)

                    // 10% chance of fastest sector / fastest lap purple colors
                    val isPurple = Random.nextInt(10) == 0

                    // Calculate gaps randomly
                    val gapStr = if (i == 0) "LEADER" else {
                        val gap = (i * 0.450 + Random.nextDouble(0.0, 0.150)).roundToDecimal(3)
                        "+$gap"
                    }

                    // Chance of position swapping
                    var posChange = 0
                    if (i > 0 && Random.nextInt(15) == 0) {
                        // Swap with driver in front
                        val temp = currentTiming[i]
                        currentTiming[i] = currentTiming[i - 1]
                        currentTiming[i - 1] = temp
                        posChange = 1
                        break
                    }

                    currentTiming[i] = driver.copy(
                        lastLapTime = lapTimeStr,
                        sector1 = s1.toString(),
                        sector2 = s2.toString(),
                        sector3 = s3.toString(),
                        isFastestLap = isPurple,
                        gapToLeader = gapStr
                    )
                }

                // Re-assign correct positions based on index
                val updatedTiming = currentTiming.mapIndexed { idx, driver ->
                    driver.copy(position = idx + 1)
                }

                liveTimingDao.clearLiveTiming()
                liveTimingDao.insertLiveTiming(updatedTiming)

                lap++
                // Ticks every 4 seconds for a dynamic UI feel
                delay(4000)
            }
        }
    }

    fun stopLiveTimingSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun Double.roundToDecimal(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    // --- Gemini-Powered Fresh F1 News Fetch ---
    // Direct REST API (Option B from guidelines)
    suspend fun generateF1NewsWithGemini(): Boolean = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("F1Repository", "Gemini API key is not configured, skipping AI news refresh.")
            // Simulate normal refresh by adding a new article randomly
            simulateMockNewsAdd()
            return@withContext true
        }

        try {
            val client = OkHttpClient.Builder().build()
            val prompt = """
                Generate 2 high-quality, realistic fictional breaking news articles for Formula 1 in July 2026.
                Return ONLY a JSON array in this exact format, with no markdown styling (do not wrap in ```json or ```):
                [
                  {
                    "id": "gemini_news_1",
                    "title": "Clean, catchy news headline",
                    "summary": "Short one-sentence summary.",
                    "content": "Fully-detailed body text of about 3 to 4 paragraphs discussing 2026 technical regulations, Hamilton's performance at Ferrari, Verstappen's fights, or McLaren's rising power.",
                    "dateStr": "Jul 01, 2026",
                    "imageUrl": "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=600",
                    "category": "breaking"
                  }
                ]
                Ensure the fields match exactly. Use high-quality Unsplash F1/racing urls for imageUrl:
                - https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7 (Red/black car)
                - https://images.unsplash.com/photo-1552519507-da3b142c6e3d (Carbon car)
                - https://images.unsplash.com/photo-1511919884226-fd3cad34687c (Sports car steering)
            """.trimIndent()

            val requestJson = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": ${JSONObject.quote(prompt)} }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "responseMimeType": "application/json"
                  }
                }
            """.trimIndent()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string() ?: ""
                Log.d("F1Repository", "Gemini response: $bodyString")

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                val textResponse = firstPart?.optString("text") ?: ""

                if (textResponse.isNotEmpty()) {
                    val articlesList = mutableListOf<NewsEntity>()
                    val articlesJson = JSONArray(textResponse)
                    for (i in 0 until articlesJson.length()) {
                        val articleObj = articlesJson.getJSONObject(i)
                        articlesList.add(
                            NewsEntity(
                                id = articleObj.optString("id", "gemini_" + System.currentTimeMillis() + "_$i"),
                                title = articleObj.optString("title"),
                                summary = articleObj.optString("summary"),
                                content = articleObj.optString("content"),
                                dateStr = articleObj.optString("dateStr"),
                                imageUrl = articleObj.optString("imageUrl"),
                                category = articleObj.optString("category")
                            )
                        )
                    }
                    if (articlesList.isNotEmpty()) {
                        newsDao.insertNews(articlesList)
                        return@withContext true
                    }
                }
            } else {
                Log.e("F1Repository", "Gemini API failed: ${response.code} ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching news with Gemini", e)
        }
        
        // Fallback simulate a news article
        simulateMockNewsAdd()
        return@withContext true
    }

    private suspend fun simulateMockNewsAdd() {
        val newArticle = NewsEntity(
            id = "sim_news_" + System.currentTimeMillis(),
            title = "Audi Formula 1 Project Speeds Up Development Ahead of 2026 Debut",
            summary = "Sauber's transitioning facility in Hinwil has completed its test rig integration for the brand new synthetic-fuel synthetic power unit.",
            content = "The future Audi F1 team, currently competing under the Stake F1 Team Kick Sauber banner, has announced the successful initial run of its complete 2026 power unit on the dynamic dyno in Neuburg. Mattia Binotto, Chief Operating Officer and Chief Technical Officer, expressed immense pride in the team's engineering efforts. 'This is an outstanding milestone in our journey towards the pinnacle of motorsport,' Binotto stated. 'With 100% sustainable fuels being mandatory for 2026, the thermodynamic challenges are vast, but our team in Neuburg has delivered an extremely robust combustion profile. Back in Hinwil, we are simultaneously optimizing the chassis and active aerodynamics systems to ensure we hit the ground running.'",
            dateStr = "Jul 01, 2026",
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?q=80&w=600",
            category = "tech"
        )
        newsDao.insertNews(listOf(newArticle))
    }
}
