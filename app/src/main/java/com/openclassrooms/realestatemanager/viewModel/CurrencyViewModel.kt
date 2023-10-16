package com.openclassrooms.realestatemanager.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrencyViewModel : ViewModel() {
    private val _isEuro = MutableLiveData<Boolean>()
    val isEuro: LiveData<Boolean>
        get() = _isEuro

    init {
        _isEuro.value = false
    }

    fun toggleCurrency() {
        _isEuro.value = !_isEuro.value!!
    }
}