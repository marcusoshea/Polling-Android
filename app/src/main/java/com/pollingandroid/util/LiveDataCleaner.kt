package com.pollingandroid.util

import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.repository.PollingOrderMemberRepository

/**
 * Utility class to clear all LiveData in repositories when users sign out
 */
object LiveDataCleaner {

    /**
     * Clears all LiveData values in repositories
     */
    fun clearAllLiveData() {
        // Clear data in PollingOrderRepository
        PollingOrderRepository.clearData()

        // Clear data in PollingOrderMemberRepository
        PollingOrderMemberRepository.clearData()

        // Add more repository clear calls as needed
    }
}