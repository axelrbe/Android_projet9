package com.openclassrooms.realestatemanager.adapters

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
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.fragments.DetailsFragment
import com.openclassrooms.realestatemanager.models.Property

class PropertyAdapter : ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DiffCallback()) {

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
            val propertyItemDesc = findViewById<TextView>(R.id.property_item_desc)
            val propertyItemPhoto = findViewById<ShapeableImageView>(R.id.property_item_photo)
            val propertyItemDate = findViewById<TextView>(R.id.property_item_date)
            val propertyItemProximityPlaces = findViewById<TextView>(R.id.property_item_proximity)
            val propertyItemAddress = findViewById<TextView>(R.id.property_item_address)
            val propertyItemStatus = findViewById<TextView>(R.id.property_item_status)

            propertyItemType.text = currentProperty.type
            "${currentProperty.price}€".also { propertyItemPrice.text = it }
            """${currentProperty.surface}m²""".also { propertyItemSurface.text = it }
            "${currentProperty.rooms} chambres".also { propertyItemRooms.text = it }
            propertyItemDesc.text = currentProperty.desc
            propertyItemDate.text = currentProperty.entryDate
            propertyItemAddress.text = currentProperty.address
            propertyItemStatus.text = currentProperty.status

            val proximityPlacesList: String = TextUtils.join(", ", currentProperty.proximityPlaces)
            propertyItemProximityPlaces.text = proximityPlacesList

            Glide.with(holder.itemView)
                .load(currentProperty.photos[0])
                .centerCrop()
                .into(propertyItemPhoto)

            var currentPhotoIndex = 0
            propertyItemPhoto.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    if (currentProperty.photos.size > 1) {
                        currentPhotoIndex = (currentPhotoIndex + 1) % currentProperty.photos.size
                        Glide.with(holder.itemView)
                            .load(currentProperty.photos[currentPhotoIndex])
                            .centerCrop()
                            .into(propertyItemPhoto)
                    }
                }
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