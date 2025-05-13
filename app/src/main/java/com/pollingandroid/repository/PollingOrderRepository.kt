package com.pollingandroid.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pollingandroid.util.UserUtils

object PollingOrderRepository {
    private val _pollingOrderName = MutableLiveData<String>().apply {
        value = UserUtils.getStoredPollingOrderName()
    }

    val pollingOrderName: LiveData<String> = _pollingOrderName

    fun clearData() {
        _pollingOrderName.postValue("")
    }

    fun updatePollingOrderName(name: String) {
        _pollingOrderName.postValue(name)
    }
}