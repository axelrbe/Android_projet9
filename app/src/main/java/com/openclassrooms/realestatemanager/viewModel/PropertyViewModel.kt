package com.openclassrooms.realestatemanager.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PropertyViewModel(private val propertyRepository: PropertyRepository, application: Application)
    : AndroidViewModel(application) {

    private val _getAllProperties = MutableStateFlow<List<Property>>(emptyList())
    private val getAllProperties: StateFlow<List<Property>> = _getAllProperties

    init {
        viewModelScope.launch {
            propertyRepository.getAllProperties().collect {
                _getAllProperties.value = it
            }
        }
    }

    fun getPropertyList(): StateFlow<List<Property>> {
        return getAllProperties
    }

    fun addProperty(property: Property) {
        viewModelScope.launch {
            propertyRepository.addProperty(property)
        }
    }
}