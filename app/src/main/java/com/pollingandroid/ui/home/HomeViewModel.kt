package com.pollingandroid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.util.UserUtils

class HomeViewModel : ViewModel() {

    val pollingOrderName: LiveData<String> = PollingOrderRepository.pollingOrderName

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${pollingOrderName.value} polling application! \n\nPlease select what you would like to view from the menu above"
    }

    val text: LiveData<String> = _text

}