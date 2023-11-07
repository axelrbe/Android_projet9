package com.openclassrooms.realestatemanager.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.openclassrooms.realestatemanager.models.Property
import kotlinx.coroutines.flow.Flow


@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: Long): Property?

    @Query("SELECT COUNT(*) FROM properties")
    fun getCount(): Int

    @Insert
    suspend fun insertProperty(property: Property)

    @Update
    suspend fun updateProperty(property: Property)

    @Delete
    suspend fun deleteProperty(property: Property)

    @Query("SELECT * FROM properties WHERE (:type IS NULL OR type = :type) AND (:minSurface IS NULL OR surface >= :minSurface) AND (:maxSurface IS NULL OR surface <= :maxSurface) AND (:minPrice IS NULL OR price >= :minPrice) AND (:maxPrice IS NULL OR price <= :maxPrice) AND (:proximityPlaces IS NULL OR proximityPlaces = :proximityPlaces) AND (:status IS NULL OR status = :status) AND (:minPhotos IS NULL OR photos = :minPhotos)")
    fun getFilteredProperties(
        type: String?,
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        proximityPlaces: List<String>?,
        status: String?,
        minPhotos: Int?
    ): List<Property>
}