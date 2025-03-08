package com.pollingandroid.ui.candidates


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.util.UserUtils

class CandidatesViewModel : ViewModel() {

    val pollingOrderName: LiveData<String> = PollingOrderRepository.pollingOrderName

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${pollingOrderName.value} CandidatesScreen"
    }

    val text: LiveData<String> = _text

}