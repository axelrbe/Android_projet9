package com.openclassrooms.realestatemanager.adapters

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.fragments.DetailsFragment
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.utils.Utils

class PropertyAdapter(var isEuro: Boolean) :
    ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DiffCallback()) {
    private var selectedPosition = RecyclerView.NO_POSITION

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyItemType = itemView.findViewById<TextView>(R.id.property_item_type)!!
        val propertyItemPrice = itemView.findViewById<TextView>(R.id.property_item_price)!!
        val propertyItemSurface = itemView.findViewById<TextView>(R.id.property_item_surface)!!
        val propertyItemRooms = itemView.findViewById<TextView>(R.id.property_item_rooms)!!
        val propertyItemImage = itemView.findViewById<ShapeableImageView>(R.id.property_item_custom_image)!!
        val propertyItemDate = itemView.findViewById<TextView>(R.id.property_item_date)!!
        val propertyItemProximityPlaces = itemView.findViewById<TextView>(R.id.property_item_proximity)!!
        val propertyItemAddress = itemView.findViewById<TextView>(R.id.property_item_address)!!
        val propertyItemStatus = itemView.findViewById<TextView>(R.id.property_item_status)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        return PropertyViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.property_item, parent, false)))
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val currentProperty = getItem(position)
        holder.itemView.apply {
            // Change colors of current property if it's tablet size
            val tabletSize = resources.getBoolean(R.bool.isTablet)
            if(tabletSize) {
                if (position == selectedPosition) {
                    holder.itemView.findViewById<LinearLayout>(R.id.property_item)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.secondaryPurple))
                    holder.itemView.findViewById<TextView>(R.id.property_item_proximity)
                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                    holder.itemView.findViewById<TextView>(R.id.property_item_address)
                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                    holder.itemView.findViewById<TextView>(R.id.property_item_rooms)
                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                    holder.itemView.findViewById<TextView>(R.id.property_item_surface)
                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                    holder.itemView.findViewById<TextView>(R.id.property_item_date)
                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    holder.itemView.findViewById<LinearLayout>(R.id.property_item)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    holder.itemView.findViewById<TextView>(R.id.property_item_proximity)
                        .setTextColor(ContextCompat.getColor(context, R.color.secondaryPurple))
                    holder.itemView.findViewById<TextView>(R.id.property_item_address)
                        .setTextColor(ContextCompat.getColor(context, R.color.secondaryPurple))
                    holder.itemView.findViewById<TextView>(R.id.property_item_rooms)
                        .setTextColor(ContextCompat.getColor(context, R.color.secondaryPurple))
                    holder.itemView.findViewById<TextView>(R.id.property_item_surface)
                        .setTextColor(ContextCompat.getColor(context, R.color.secondaryPurple))
                    holder.itemView.findViewById<TextView>(R.id.property_item_date)
                        .setTextColor(ContextCompat.getColor(context, R.color.secondaryPurple))
                }
            }

            // Binding infos to the views
            holder.propertyItemType.text = currentProperty.type
            """${currentProperty.surface}m²""".also { holder.propertyItemSurface.text = it }
            (currentProperty.rooms.toString() + " " + context.getString(R.string.rooms)).also {
                holder.propertyItemRooms.text = it
            }
            (context.getString(R.string.since) + " " + currentProperty.entryDate).also {
                holder.propertyItemDate.text = it
            }
            holder.propertyItemAddress.text = currentProperty.address
            holder.propertyItemStatus.text = currentProperty.status

            val price = if (isEuro) {
                Utils.convertDollarToEuro(currentProperty.price).toString() + "€"
            } else {
                "$" + (currentProperty.price).toString()
            }
            holder.propertyItemPrice.text = price

            val proximityPlacesList: String = TextUtils.join(", ", currentProperty.proximityPlaces)
            holder.propertyItemProximityPlaces.text = proximityPlacesList

            Glide.with(holder.itemView)
                .load(currentProperty.photos[0].uri)
                .centerCrop()
                .into(holder.propertyItemImage)

            var currentPhotoIndex = 0
            holder.propertyItemImage.setOnClickListener {
                if (currentProperty.photos.size > 1) {
                    currentPhotoIndex = (currentPhotoIndex + 1) % currentProperty.photos.size
                    Glide.with(holder.itemView)
                        .load(currentProperty.photos[currentPhotoIndex].uri)
                        .centerCrop()
                        .into(holder.propertyItemImage)
                }
            }

            // Details fragment management
            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.bindingAdapterPosition
                notifyItemChanged(selectedPosition)
                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition)
                }
                val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                if (tabletSize) {
                    val detailFragment = DetailsFragment.newInstance(currentProperty)
                    fragmentManager.beginTransaction()
                        .replace(R.id.detail_fragment_container, detailFragment)
                        .commit()
                } else {
                    val detailsFragment = DetailsFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable("property", currentProperty)
                        }
                    }

                    fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, detailsFragment)
                        .addToBackStack("PropertyListFragment")
                        .commit()
                }
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