package com.openclassrooms.realestatemanager.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
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
    val location: LatLng?,
    val proximityPlaces: List<String>,
    val status: String,
    val entryDate: String,
    val soldDate: Date?,
    val realEstateAgent: String
) : Parcelable