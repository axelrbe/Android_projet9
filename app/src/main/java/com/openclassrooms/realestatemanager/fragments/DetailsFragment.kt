package com.openclassrooms.realestatemanager.fragments

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDetailsBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.utils.Utils
import com.openclassrooms.realestatemanager.viewModel.CurrencyViewModel

class DetailsFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailsBinding? = null
    private lateinit var propertyListFragment: PropertyListFragment
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var currencyViewModel: CurrencyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currencyViewModel = ViewModelProvider(requireActivity())[CurrencyViewModel::class.java]

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
        "${property?.surface.toString()}m²".also { binding.detailsPropertySurface.text = it }
        binding.detailsPropertyRooms.text = property?.rooms.toString()
        binding.detailsPropertyDesc.text = property?.desc
        binding.detailsPropertyEntryDate.text = property?.entryDate
        binding.detailsPropertyAddress.text = property?.address
        binding.detailsPropertyRealEstateAgent.text = property?.realEstateAgent

        currencyViewModel.isEuro.observe(viewLifecycleOwner) { isEuro ->
            val price = if (isEuro) {
                Utils.convertDollarToEuro(property?.price ?: 0).toString() + "€"
            } else {
                "$" + (property?.price ?: 0).toString()
            }
            binding.detailsPropertyPrice.text = price
        }


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

        showCustomViewForImage(property)

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

    private fun showCustomViewForImage(property: Property?) {
        val namedImageView = binding.detailsPropertyPhotosCustomImageView
        if (!property?.photos.isNullOrEmpty()) {
            for (photo in property?.photos!!) {
                namedImageView.setImageUri(Uri.parse(photo.uri))
                namedImageView.setText(photo.name)
            }
        }

        val nextPhotoBtn = binding.detailsPropertyPhotosNextBtn
        var currentPhotoIndex = 0
        nextPhotoBtn.setOnClickListener {
            if (property?.photos?.size!! > (currentPhotoIndex + 1)) {
                currentPhotoIndex++
            } else {
                currentPhotoIndex = 0
            }
            namedImageView.setImageUri(Uri.parse(property.photos[currentPhotoIndex].uri))
            property.photos[currentPhotoIndex].name.let { it1 -> namedImageView.setText(it1) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}