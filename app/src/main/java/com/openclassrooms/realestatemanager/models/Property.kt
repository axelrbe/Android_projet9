package com.openclassrooms.realestatemanager.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import com.openclassrooms.realestatemanager.utils.Converters
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    var price: Long,
    var surface: Long,
    var rooms: Int,
    var desc: String,
    val address: String,
    val location: LatLng?,
    var proximityPlaces: List<String>,
    var status: String,
    val entryDate: String,
    var soldDate: String,
    var realEstateAgent: String,
    @TypeConverters(Converters.PhotoListConverter::class)
    val photos: List<Photo>
) : Parcelable {
    @Parcelize
    data class Photo(
        val uri: String,
        var name: String
    ) : Parcelable
}