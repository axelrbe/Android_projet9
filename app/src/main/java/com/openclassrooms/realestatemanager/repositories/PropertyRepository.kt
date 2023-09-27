package com.openclassrooms.realestatemanager.repositories

import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.Property
import kotlinx.coroutines.flow.Flow

class PropertyRepository(private val propertyDao: PropertyDao) {
    fun getAllProperties(): Flow<List<Property>> {
        return propertyDao.getAllProperties()
    }

    suspend fun addProperty(property: Property) {
        propertyDao.insertProperty(property)
    }
}
