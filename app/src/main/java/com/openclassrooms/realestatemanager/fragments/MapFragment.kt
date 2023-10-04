package com.openclassrooms.realestatemanager.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModel
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MapFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentMapBinding? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var propertyDao: PropertyDao
    private lateinit var propertyViewModel: PropertyViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapFragmentArrowBack.setOnClickListener {
            val propertyListFragment = PropertyListFragment()
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, propertyListFragment)
                .commit()
        }

        addMarkersToMap()
        moveCameraToTheCurrentLocation()
    }

    private fun addMarkersToMap() {
        propertyDao = RealEstateApplication.getInstance(requireContext()).propertyDao()
        propertyRepository = PropertyRepository(propertyDao)
        propertyViewModel = ViewModelProvider(
            this,
            PropertyViewModelFactory(propertyRepository, requireActivity().application)
        )[PropertyViewModel::class.java]
        propertyViewModel.getPropertyList().onEach { properties ->
            mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
            mapFragment.getMapAsync { map ->
                googleMap = map
                googleMap.clear()
                properties.forEach { property ->
                    property.location?.let {
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .title(property.type)
                                .snippet(property.address)
                                .position(it)
                        )
                        marker?.tag = property
                        googleMap.setOnInfoWindowClickListener { clickedMarker ->
                            val clickedProperty = clickedMarker.tag as Property
                            val detailsFragment = DetailsFragment().apply {
                                arguments = Bundle().apply {
                                    putParcelable("property", clickedProperty)
                                }
                            }
                            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                            fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, detailsFragment)
                                .commit()
                        }
                    }
                    googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                        override fun getInfoWindow(marker: Marker): View {
                            val view = layoutInflater.inflate(
                                R.layout.custom_info_window,
                                mapFragment.view as ViewGroup,
                                false
                            )
                            view.findViewById<TextView>(R.id.text_view_type).text = property.type
                            view.findViewById<TextView>(R.id.text_view_address).text = property.address
                            (property.surface.toString() + "m²").also {
                                view.findViewById<TextView>(R.id.text_view_surface).text = it
                            }
                            (property.price.toString() + "€").also {
                                view.findViewById<TextView>(R.id.text_view_price).text = it
                            }
                            return view
                        }

                        override fun getInfoContents(marker: Marker): View? {
                            return null
                        }
                    })
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun moveCameraToTheCurrentLocation() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        googleMap.isMyLocationEnabled = true
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        ), 15f
                                    )
                                )
                            }
                        }
                    }
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        googleMap.isMyLocationEnabled = true
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        ), 15f
                                    )
                                )
                            }
                        }
                    }
                }

                else -> {
                    Toast.makeText(requireContext(), getString(R.string.location_permission), Toast.LENGTH_LONG).show()
                }
            }
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
