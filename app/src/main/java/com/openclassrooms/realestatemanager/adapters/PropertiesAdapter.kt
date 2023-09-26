package com.openclassrooms.realestatemanager.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Property

class PropertiesAdapter(private val properties: MutableList<Property>) : RecyclerView.Adapter<PropertiesAdapter.PropertiesViewHolder>(){

    class PropertiesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertiesViewHolder {
        return PropertiesViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.property_item, parent, false)))
    }

    override fun onBindViewHolder(holder: PropertiesViewHolder, position: Int) {
        val currentProperty = properties[position]
        holder.itemView.apply {
            val propertyItemType = findViewById<TextView>(R.id.property_item_type)
            val propertyItemPrice = findViewById<TextView>(R.id.property_item_price)
            val propertyItemSurface = findViewById<TextView>(R.id.property_item_surface)
            val propertyItemRooms = findViewById<TextView>(R.id.property_item_rooms)
            val propertyItemDesc = findViewById<TextView>(R.id.property_item_desc)
            val propertyItemPhoto = findViewById<ShapeableImageView>(R.id.property_item_photo)
            val propertyItemDate = findViewById<TextView>(R.id.property_item_date)
            val propertyItemProximityPlaces = findViewById<TextView>(R.id.property_item_proximity)
            val propertyItemAddress = findViewById<TextView>(R.id.property_item_address)
            val propertyItemStatus = findViewById<TextView>(R.id.property_item_status)

            propertyItemType.text = currentProperty.type
            "${currentProperty.price}€".also { propertyItemPrice.text = it }
            """${currentProperty.surface}m²""".also { propertyItemSurface.text = it }
            "${currentProperty.numberOfRooms} Chambres".also { propertyItemRooms.text = it }
            propertyItemDesc.text = currentProperty.desc
            propertyItemDate.text = currentProperty.entryDate
            propertyItemAddress.text = currentProperty.address
            propertyItemStatus.text = currentProperty.status

            val proximityPlacesList: String = TextUtils.join(", ", currentProperty.proximityPlaces)
            propertyItemProximityPlaces.text = proximityPlacesList

            currentProperty.photos.firstOrNull()?.let { photoUri ->
                propertyItemPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                propertyItemPhoto.load(photoUri)
            }
        }
    }

    override fun getItemCount(): Int {
        return properties.size
    }
}