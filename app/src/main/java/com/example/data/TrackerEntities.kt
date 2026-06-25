package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trackers"
)
data class Tracker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "COUNTER", "BOOLEAN", "QUANTITY", "TIMER"
    val unit: String, // e.g. "times", "ml", "mins", "rating"
    val iconName: String, // e.g. "DirectionsWalk", "LocalLaundryService", "LocalDrink", "Wc", "Bedtime", "FitnessCenter", "MedicalServices", "Restaurant", "Star"
    val colorHex: String, // e.g. "#4CAF50"
    val targetValue: Float? = null, // Optional daily goal
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tracker_logs",
    foreignKeys = [
        ForeignKey(
            entity = Tracker::class,
            parentColumns = ["id"],
            childColumns = ["trackerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trackerId"])]
)
data class TrackerLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackerId: Long,
    val value: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
