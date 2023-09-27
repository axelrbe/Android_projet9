package com.openclassrooms.realestatemanager.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val price: Long,
    val surface: Long,
    val rooms: Int,
    val desc: String,
    val photos: List<String>,
    val address: String,
    val proximityPlaces: List<String>,
    val status: String,
    val entryDate: String,
    val soldDate: Date?,
    val realEstateAgent: String
)