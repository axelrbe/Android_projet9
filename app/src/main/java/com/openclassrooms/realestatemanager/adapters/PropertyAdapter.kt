package com.openclassrooms.realestatemanager.adapters

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.customView.NamedImageView
import com.openclassrooms.realestatemanager.fragments.DetailsFragment
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.utils.Utils

class PropertyAdapter(var isEuro: Boolean) :
    ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DiffCallback()) {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        return PropertyViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.property_item, parent, false)))
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val currentProperty = getItem(position)
        holder.itemView.apply {
            val propertyItemType = findViewById<TextView>(R.id.property_item_type)
            val propertyItemPrice = findViewById<TextView>(R.id.property_item_price)
            val propertyItemSurface = findViewById<TextView>(R.id.property_item_surface)
            val propertyItemRooms = findViewById<TextView>(R.id.property_item_rooms)
            val propertyItemImage = findViewById<NamedImageView>(R.id.property_item_custom_image)
            val propertyItemDate = findViewById<TextView>(R.id.property_item_date)
            val propertyItemProximityPlaces = findViewById<TextView>(R.id.property_item_proximity)
            val propertyItemAddress = findViewById<TextView>(R.id.property_item_address)
            val propertyItemStatus = findViewById<TextView>(R.id.property_item_status)

            propertyItemType.text = currentProperty.type
            """${currentProperty.surface}m²""".also { propertyItemSurface.text = it }
            (currentProperty.rooms.toString() + " " + context.getString(R.string.rooms)).also {
                propertyItemRooms.text = it
            }
            (context.getString(R.string.since) + " " + currentProperty.entryDate).also { propertyItemDate.text = it }
            propertyItemAddress.text = currentProperty.address
            propertyItemStatus.text = currentProperty.status

            val price = if (isEuro) {
                Utils.convertDollarToEuro(currentProperty.price).toString() + "€"
            } else {
                "$" + (currentProperty.price).toString()
            }
            propertyItemPrice.text = price

            val proximityPlacesList: String = TextUtils.join(", ", currentProperty.proximityPlaces)
            propertyItemProximityPlaces.text = proximityPlacesList

            if (!currentProperty?.photos.isNullOrEmpty()) {
                for (photo in currentProperty?.photos!!) {
                    propertyItemImage.setImageUri(Uri.parse(photo.uri))
                    propertyItemImage.setText(photo.name)
                }
            }

            var currentPhotoIndex = 0
            propertyItemImage.setOnClickListener {
                if (currentProperty?.photos?.size!! > (currentPhotoIndex + 1)) {
                    currentPhotoIndex++
                } else {
                    currentPhotoIndex = 0
                }
                propertyItemImage.setImageUri(Uri.parse(currentProperty.photos[currentPhotoIndex].uri))
                currentProperty.photos[currentPhotoIndex].name.let { it1 -> propertyItemImage.setText(it1) }
            }

            holder.itemView.setOnClickListener {
                val detailsFragment = DetailsFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("property", currentProperty)
                    }
                }

                val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack("PropertyListFragment")
                    .commit()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}