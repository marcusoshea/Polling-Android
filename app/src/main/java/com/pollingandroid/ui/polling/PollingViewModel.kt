package com.pollingandroid.ui.polling


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.util.UserUtils

class PollingViewModel : ViewModel() {

    val pollingOrderName: LiveData<String> = PollingOrderRepository.pollingOrderName


    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${pollingOrderName.value} PollingScreen"
    }

    val text: LiveData<String> = _text

}