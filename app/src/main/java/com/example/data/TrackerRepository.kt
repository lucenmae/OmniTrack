package com.example.data

import kotlinx.coroutines.flow.Flow

class TrackerRepository(private val trackerDao: TrackerDao) {
    val allTrackers: Flow<List<Tracker>> = trackerDao.getAllTrackersFlow()
    val allLogs: Flow<List<TrackerLog>> = trackerDao.getAllLogsFlow()

    suspend fun getTrackerById(id: Long): Tracker? {
        return trackerDao.getTrackerById(id)
    }

    suspend fun insertTracker(tracker: Tracker): Long {
        return trackerDao.insertTracker(tracker)
    }

    suspend fun updateTracker(tracker: Tracker) {
        trackerDao.updateTracker(tracker)
    }

    suspend fun deleteTracker(tracker: Tracker) {
        trackerDao.deleteTracker(tracker)
    }

    fun getLogsForTracker(trackerId: Long): Flow<List<TrackerLog>> {
        return trackerDao.getLogsForTrackerFlow(trackerId)
    }

    fun getLogsSince(sinceTimestamp: Long): Flow<List<TrackerLog>> {
        return trackerDao.getLogsSinceFlow(sinceTimestamp)
    }

    suspend fun insertLog(log: TrackerLog): Long {
        return trackerDao.insertLog(log)
    }

    suspend fun updateLog(log: TrackerLog) {
        trackerDao.updateLog(log)
    }

    suspend fun deleteLog(log: TrackerLog) {
        trackerDao.deleteLog(log)
    }

    suspend fun deleteLogById(logId: Long) {
        trackerDao.deleteLogById(logId)
    }
}
