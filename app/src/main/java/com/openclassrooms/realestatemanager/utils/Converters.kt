package com.openclassrooms.realestatemanager.utils

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date


class Converters {
    class DateTypeConverter {
        @TypeConverter
        fun fromTimestamp(value: Long?): Date? {
            return value?.let { Date(it) }
        }

        @TypeConverter
        fun dateToTimestamp(date: Date?): Long? {
            return date?.time
        }
    }
    class StringListTypeConverter {
        @TypeConverter
        fun fromString(value: String?): List<String>? {
            val listType = object : TypeToken<List<String>?>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        fun fromList(list: List<String>?): String? {
            return Gson().toJson(list)
        }
    }

    class LatLngTypeConverter {
        @TypeConverter
        fun fromLatLng(latLng: LatLng?): String? {
            return latLng?.let { "${it.latitude},${it.longitude}" }
        }

        @TypeConverter
        fun toLatLng(latLngString: String?): LatLng? {
            return latLngString?.let {
                val parts = it.split(",")
                LatLng(parts[0].toDouble(), parts[1].toDouble())
            }
        }
    }
}