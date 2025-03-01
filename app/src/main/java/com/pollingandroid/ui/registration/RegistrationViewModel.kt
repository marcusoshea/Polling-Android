package com.pollingandroid.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.model.PollingOrder

class RegistrationViewModel : ViewModel() {


    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _pollingOrders = MutableLiveData<List<PollingOrder>>()
    val pollingOrders: LiveData<List<PollingOrder>> = _pollingOrders

    private val _selectedPollingOrder = MutableLiveData<PollingOrder?>()
    val selectedPollingOrder: LiveData<PollingOrder?> = _selectedPollingOrder

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setName(name: String) {
        _name.value = name
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setPollingOrders(orders: List<PollingOrder>) {
        _pollingOrders.value = orders
    }

    fun setSelectedPollingOrder(order: PollingOrder?) {
        _selectedPollingOrder.value = order
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
}