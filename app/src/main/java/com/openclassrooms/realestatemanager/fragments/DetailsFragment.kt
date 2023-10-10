package com.openclassrooms.realestatemanager.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDetailsBinding
import com.openclassrooms.realestatemanager.models.Property

class DetailsFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailsBinding? = null
    private lateinit var propertyListFragment: PropertyListFragment
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPropertyData()
        binding.arrowBackBtn.setOnClickListener {
            propertyListFragment = PropertyListFragment()
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.popBackStack()
        }
    }

    private fun loadPropertyData() {
        val property = arguments?.getParcelable<Property>("property")

        binding.detailsPropertyType.text = property?.type
        "${property?.price.toString()}€".also { binding.detailsPropertyPrice.text = it }
        "${property?.surface.toString()}m²".also { binding.detailsPropertySurface.text = it }
        binding.detailsPropertyRooms.text = property?.rooms.toString()
        binding.detailsPropertyDesc.text = property?.desc
        binding.detailsPropertyEntryDate.text = property?.entryDate
        binding.detailsPropertyAddress.text = property?.address
        binding.detailsPropertyRealEstateAgent.text = property?.realEstateAgent

        // Add the proximity places to the layout
        val selectedItems = property?.proximityPlaces ?: emptyList()
        val linearLayout = binding.detailsProximityPlacesLayout
        linearLayout.removeAllViews()
        for (selectedItem in selectedItems) {
            val textView = TextView(requireContext())
            textView.text = selectedItem
            textView.setPadding(10, 10, 10, 10)
            textView.textSize = 20F
            textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            textView.setTypeface(null, Typeface.BOLD)
            textView.gravity = Gravity.CENTER_HORIZONTAL
            linearLayout.addView(textView)
        }

        // Add photos to image view + next photo btn management
        val imageView = binding.detailsPropertyPhotosImageView
        if (!property?.photos.isNullOrEmpty()) {
            Glide.with(this)
                .load(property?.photos?.get(0))
                .centerCrop()
                .into(imageView)
        }
        val nextPhotoBtn = binding.detailsPropertyPhotosNextBtn
        var currentPhotoIndex = 0
        nextPhotoBtn.setOnClickListener {
            if (property?.photos != null) {
                if (property.photos.size > (currentPhotoIndex + 1)) {
                    currentPhotoIndex++
                } else {
                    currentPhotoIndex = 0
                }
                Glide.with(this)
                    .load(property.photos[currentPhotoIndex])
                    .centerCrop()
                    .into(imageView)

            }
        }

        // Google map implementation
        mapFragment = childFragmentManager.findFragmentById(R.id.confirmation_map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.clear()
            property?.location?.let {
                googleMap.addMarker(MarkerOptions().position(it).title(property.address))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}