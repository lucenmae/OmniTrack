package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM trackers ORDER BY isArchived ASC, createdAt DESC")
    fun getAllTrackersFlow(): Flow<List<Tracker>>

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerById(id: Long): Tracker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: Tracker): Long

    @Update
    suspend fun updateTracker(tracker: Tracker)

    @Delete
    suspend fun deleteTracker(tracker: Tracker)

    @Query("SELECT * FROM tracker_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<TrackerLog>>

    @Query("SELECT * FROM tracker_logs WHERE trackerId = :trackerId ORDER BY timestamp DESC")
    fun getLogsForTrackerFlow(trackerId: Long): Flow<List<TrackerLog>>

    @Query("SELECT * FROM tracker_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getLogsSinceFlow(sinceTimestamp: Long): Flow<List<TrackerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TrackerLog): Long

    @Update
    suspend fun updateLog(log: TrackerLog)

    @Delete
    suspend fun deleteLog(log: TrackerLog)

    @Query("DELETE FROM tracker_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)
}
