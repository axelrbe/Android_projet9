package com.openclassrooms.realestatemanager.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.utils.Converters

@Database(entities = [Property::class], version = 1)
@TypeConverters(Converters.DateTypeConverter::class, Converters.StringListTypeConverter::class, Converters.LatLngTypeConverter::class)
abstract class PropertyDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
}