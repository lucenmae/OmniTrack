package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TrackerWithStats(
    val tracker: Tracker,
    val todayValue: Float,
    val lastLoggedAt: Long?,
    val streakDays: Int,
    val weeklyHistory: List<DailyLogSummary>
)

data class DailyLogSummary(
    val dayLabel: String,
    val value: Float,
    val isTargetMet: Boolean
)

data class ActivityLogItem(
    val logId: Long,
    val trackerId: Long,
    val trackerName: String,
    val trackerColor: String,
    val trackerIcon: String,
    val trackerType: String,
    val loggedValue: Float,
    val unit: String,
    val timestamp: Long,
    val note: String?
)

class TrackerViewModel(private val repository: TrackerRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            // Check if trackers are empty and populate with beautiful defaults
            val existing = repository.allTrackers.first()
            if (existing.isEmpty()) {
                repository.insertTracker(
                    Tracker(
                        name = "Walking",
                        type = "QUANTITY",
                        unit = "steps",
                        iconName = "DirectionsWalk",
                        colorHex = "#4CAF50",
                        targetValue = 10000f
                    )
                )
                repository.insertTracker(
                    Tracker(
                        name = "Water Intake",
                        type = "QUANTITY",
                        unit = "ml",
                        iconName = "LocalDrink",
                        colorHex = "#2196F3",
                        targetValue = 2000f
                    )
                )
                repository.insertTracker(
                    Tracker(
                        name = "Poop",
                        type = "COUNTER",
                        unit = "times",
                        iconName = "Wc",
                        colorHex = "#795548",
                        targetValue = 1f
                    )
                )
                repository.insertTracker(
                    Tracker(
                        name = "Laundry Done",
                        type = "COUNTER",
                        unit = "loads",
                        iconName = "LocalLaundryService",
                        colorHex = "#00BCD4",
                        targetValue = null
                    )
                )
                repository.insertTracker(
                    Tracker(
                        name = "Sleep Duration",
                        type = "QUANTITY",
                        unit = "hours",
                        iconName = "Bedtime",
                        colorHex = "#9C27B0",
                        targetValue = 8f
                    )
                )
            }
        }
    }

    val trackersWithStats: StateFlow<List<TrackerWithStats>> = combine(
        repository.allTrackers,
        repository.allLogs
    ) { trackers, logs ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        trackers.map { tracker ->
            val trackerLogs = logs.filter { it.trackerId == tracker.id }
            val logsToday = trackerLogs.filter { it.timestamp >= startOfToday }
            val todayValue = logsToday.sumOf { it.value.toDouble() }.toFloat()

            val lastLog = trackerLogs.firstOrNull()
            val lastLoggedAt = lastLog?.timestamp

            val streakDays = calculateStreak(trackerLogs)

            val weeklyHistory = (0..6).reversed().map { daysAgo ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                val dayEnd = dayStart + 86400000

                val dayLogs = trackerLogs.filter { it.timestamp in dayStart until dayEnd }
                val daySum = dayLogs.sumOf { it.value.toDouble() }.toFloat()

                val dayFormat = SimpleDateFormat("EE", Locale.getDefault())
                val dayLabel = dayFormat.format(cal.time).first().toString()

                DailyLogSummary(
                    dayLabel = dayLabel,
                    value = daySum,
                    isTargetMet = tracker.targetValue?.let { daySum >= it } ?: (daySum > 0)
                )
            }

            TrackerWithStats(
                tracker = tracker,
                todayValue = todayValue,
                lastLoggedAt = lastLoggedAt,
                streakDays = streakDays,
                weeklyHistory = weeklyHistory
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivity: StateFlow<List<ActivityLogItem>> = combine(
        repository.allTrackers,
        repository.allLogs
    ) { trackers, logs ->
        val trackerMap = trackers.associateBy { it.id }
        logs.mapNotNull { log ->
            val tracker = trackerMap[log.trackerId] ?: return@mapNotNull null
            ActivityLogItem(
                logId = log.id,
                trackerId = tracker.id,
                trackerName = tracker.name,
                trackerColor = tracker.colorHex,
                trackerIcon = tracker.iconName,
                trackerType = tracker.type,
                loggedValue = log.value,
                unit = tracker.unit,
                timestamp = log.timestamp,
                note = log.note
            )
        }.take(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCompletionRate: StateFlow<Float> = trackersWithStats.map { list ->
        val trackersWithTargets = list.filter { it.tracker.targetValue != null }
        if (trackersWithTargets.isEmpty()) {
            val activeCount = list.count { it.todayValue > 0 }
            if (list.isEmpty()) 0f else activeCount.toFloat() / list.size
        } else {
            val completedTargets = trackersWithTargets.count { it.todayValue >= (it.tracker.targetValue ?: 0f) }
            completedTargets.toFloat() / trackersWithTargets.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun addTracker(name: String, type: String, unit: String, iconName: String, colorHex: String, targetValue: Float?) {
        viewModelScope.launch {
            repository.insertTracker(
                Tracker(
                    name = name,
                    type = type,
                    unit = unit,
                    iconName = iconName,
                    colorHex = colorHex,
                    targetValue = targetValue
                )
            )
        }
    }

    fun updateTracker(tracker: Tracker) {
        viewModelScope.launch {
            repository.updateTracker(tracker)
        }
    }

    fun deleteTracker(tracker: Tracker) {
        viewModelScope.launch {
            repository.deleteTracker(tracker)
        }
    }

    fun logActivity(trackerId: Long, value: Float, note: String? = null) {
        viewModelScope.launch {
            repository.insertLog(
                TrackerLog(
                    trackerId = trackerId,
                    value = value,
                    note = note
                )
            )
        }
    }

    fun deleteLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteLogById(logId)
        }
    }

    private fun calculateStreak(logs: List<TrackerLog>): Int {
        if (logs.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val loggedDays = logs.map { sdf.format(Date(it.timestamp)) }
            .distinct()
            .sortedDescending()

        if (loggedDays.isEmpty()) return 0

        val todayStr = sdf.format(Date())
        val yesterdayStr = sdf.format(Date(System.currentTimeMillis() - 86400000))

        val latestDay = loggedDays.first()
        if (latestDay != todayStr && latestDay != yesterdayStr) {
            return 0
        }

        var streak = 0
        var currentCheckTime = if (latestDay == todayStr) System.currentTimeMillis() else System.currentTimeMillis() - 86400000

        for (day in loggedDays) {
            val checkDayStr = sdf.format(Date(currentCheckTime))
            if (day == checkDayStr) {
                streak++
                currentCheckTime -= 86400000
            } else {
                break
            }
        }
        return streak
    }

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = TrackerDatabase.getDatabase(context)
                val repository = TrackerRepository(database.trackerDao())
                return TrackerViewModel(repository) as T
            }
        }
    }
}
