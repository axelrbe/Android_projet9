package com.openclassrooms.realestatemanager.contentProvider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.room.Room
import com.google.android.gms.maps.model.LatLng
import com.openclassrooms.realestatemanager.database.PropertyDatabase
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.Property
import kotlinx.coroutines.runBlocking


class RealEstateManagerContentProvider : ContentProvider() {

    private lateinit var propertyDao: PropertyDao

    companion object {
        private const val AUTHORITY = "com.openclassrooms.realestatemanager.provider"
        private const val PATH_PROPERTIES = "properties"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_PROPERTIES")
    }

    override fun onCreate(): Boolean {
        val database = Room.databaseBuilder(
            context!!.applicationContext,
            PropertyDatabase::class.java,
            "property_database"
        ).build()
        propertyDao = database.propertyDao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val properties = propertyDao.getFilteredProperties(
            type = uri.getQueryParameter("type"),
            minSurface = uri.getQueryParameter("minSurface")?.toIntOrNull(),
            maxSurface = uri.getQueryParameter("maxSurface")?.toIntOrNull(),
            minPrice = uri.getQueryParameter("minPrice")?.toIntOrNull(),
            maxPrice = uri.getQueryParameter("maxPrice")?.toIntOrNull(),
            proximityPlaces = uri.getQueryParameter("proximityPlaces")?.split(","),
            status = uri.getQueryParameter("status"),
            minPhotos = uri.getQueryParameter("minPhotos")?.toIntOrNull()
        )
        val cursor = MatrixCursor(
            arrayOf(
                "_id", "type", "price", "surface", "rooms", "desc", "address", "latitude", "longitude", "status",
                "entryDate", "soldDate", "realEstateAgent"
            )
        )
        properties.forEach {
            cursor.addRow(
                arrayOf(
                    it.id,
                    it.type,
                    it.price,
                    it.surface,
                    it.rooms,
                    it.desc,
                    it.address,
                    it.location?.latitude,
                    it.location?.longitude,
                    it.status,
                    it.entryDate,
                    it.soldDate,
                    it.realEstateAgent
                )
            )
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val locationString = values?.getAsString("location")
        val locationParts = locationString?.split(",")
        val location = if (locationParts?.size == 2) {
            LatLng(locationParts[0].toDoubleOrNull() ?: 0.0, locationParts[1].toDoubleOrNull() ?: 0.0)
        } else {
            null
        }
        val property = Property(
            type = values?.getAsString("type") ?: "",
            price = values?.getAsLong("price") ?: 0,
            surface = values?.getAsLong("surface") ?: 0,
            rooms = values?.getAsInteger("rooms") ?: 0,
            desc = values?.getAsString("desc") ?: "",
            address = values?.getAsString("address") ?: "",
            location = location,
            proximityPlaces = values?.getAsString("proximityPlaces")?.split(",") ?: emptyList(),
            status = values?.getAsString("status") ?: "",
            entryDate = values?.getAsString("entryDate") ?: "",
            soldDate = values?.getAsString("soldDate") ?: "",
            realEstateAgent = values?.getAsString("realEstateAgent") ?: "",
            photos = emptyList()
        )
        runBlocking {
            propertyDao.insertProperty(property)
        }
        context!!.contentResolver.notifyChange(uri, null)
        return Uri.withAppendedPath(CONTENT_URI, property.id.toString())
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val locationString = values?.getAsString("location")
        val locationParts = locationString?.split(",")
        val location = if (locationParts?.size == 2) {
            LatLng(locationParts[0].toDoubleOrNull() ?: 0.0, locationParts[1].toDoubleOrNull() ?: 0.0)
        } else {
            null
        }
        val property = Property(
            id = uri.lastPathSegment?.toLongOrNull() ?: 0,
            type = values?.getAsString("type") ?: "",
            price = values?.getAsLong("price") ?: 0,
            surface = values?.getAsLong("surface") ?: 0,
            rooms = values?.getAsInteger("rooms") ?: 0,
            desc = values?.getAsString("desc") ?: "",
            address = values?.getAsString("address") ?: "",
            location = location,
            proximityPlaces = values?.getAsString("proximityPlaces")?.split(",") ?: emptyList(),
            status = values?.getAsString("status") ?: "",
            entryDate = values?.getAsString("entryDate") ?: "",
            soldDate = values?.getAsString("soldDate") ?: "",
            realEstateAgent = values?.getAsString("realEstateAgent") ?: "",
            photos = emptyList()
        )
        runBlocking {
            propertyDao.updateProperty(property)
        }
        context!!.contentResolver.notifyChange(uri, null)
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val id = uri.lastPathSegment?.toLongOrNull() ?: 0
        val property = runBlocking {
            propertyDao.getPropertyById(id)
        }
        return if (property != null) {
            runBlocking {
                propertyDao.deleteProperty(property)
            }
            context!!.contentResolver.notifyChange(uri, null)
            1
        } else {
            0
        }
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/vnd.com.openclassrooms.realestatemanager.provider.properties"
    }
}