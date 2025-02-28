package com.pollingandroidv2.ui.polling


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.polling_android.util.UserUtils

class PollingViewModel : ViewModel() {

    private val _pollingOrderName = MutableLiveData<String>().apply {
        value = UserUtils.getStoredPollingOrderName()
    }

    val pollingOrderName: LiveData<String> = _pollingOrderName

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${_pollingOrderName.value} PollingScreen"
    }

    val text: LiveData<String> = _text

}