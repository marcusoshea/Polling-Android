package com.pollingandroid.ui.report


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.util.UserUtils

class ReportViewModel : ViewModel() {

    private val _pollingOrderName = MutableLiveData<String>().apply {
        value = UserUtils.getStoredPollingOrderName()
    }

    val pollingOrderName: LiveData<String> = _pollingOrderName

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${_pollingOrderName.value} ReportScreen"
    }

    val text: LiveData<String> = _text

}