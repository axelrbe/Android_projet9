package com.openclassrooms.realestatemanager.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.repositories.PropertyRepository

class PropertyViewModelFactory(private val propertyRepository: PropertyRepository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) {
            return PropertyViewModel(propertyRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}