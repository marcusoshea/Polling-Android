package com.pollingandroid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.util.UserUtils

class HomeViewModel : ViewModel() {

    private val _pollingOrderName = MutableLiveData<String>().apply {
        value = UserUtils.getStoredPollingOrderName()
    }

    val pollingOrderName: LiveData<String> = _pollingOrderName

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${_pollingOrderName.value} polling application! \n\nPlease select what you would like to view from the menu above"
    }

    val text: LiveData<String> = _text

}