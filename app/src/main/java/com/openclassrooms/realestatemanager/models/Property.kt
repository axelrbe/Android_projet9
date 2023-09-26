package com.openclassrooms.realestatemanager.models

import android.net.Uri
import java.util.Date

data class Property(val type: String, val price: Long, val surface: Long, val numberOfRooms: Int, val desc: String,
val photos: List<Uri?>, val address: String, val proximityPlaces: List<String>, val status: String,
                    val entryDate: String, val sellDate: Date?, val realEstateAgent: String)