package com.openclassrooms.realestatemanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.adapters.PropertyAdapter
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.databinding.FragmentPropertyListBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModel
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PropertyListFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentPropertyListBinding? = null
    private lateinit var propertyAdapter: PropertyAdapter
    private lateinit var formFragment: FormFragment
    private lateinit var propertyList: List<Property>
    private lateinit var propertyViewModel: PropertyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPropertyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        propertyAdapter = PropertyAdapter()
        val propertyDao = RealEstateApplication.getInstance(requireContext()).propertyDao()
        val propertyRepository = PropertyRepository(propertyDao)
        propertyViewModel = ViewModelProvider(
            this,
            PropertyViewModelFactory(propertyRepository, requireActivity().application)
        )[PropertyViewModel::class.java]

        // Set up RecyclerView and adapter
        binding.propertiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = propertyAdapter
        }

        // Show the initial list of properties
        setPropertyList()

        // Reset the filters
        binding.resetFilterBtn.setOnClickListener {
            setPropertyList()
            binding.resetFilterBtn.visibility = View.GONE
        }

        showFormFragment()
        showFilterFragment()
        showMapFragment()
    }

    private fun setPropertyList() {
        propertyViewModel.getPropertyList().onEach { properties ->
            propertyList = properties
            propertyAdapter.submitList(propertyList)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    fun applyFilters(
        type: String?,
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        proximityPlaces: List<String>?,
        status: String?,
        minPhotos: Int?
    ) {
        val filteredList = propertyList.filter { property ->
            (type == null || property.type == type) &&
                    (minSurface == null || property.surface >= minSurface) &&
                    (maxSurface == null || property.surface <= maxSurface) &&
                    (minPrice == null || property.price >= minPrice) &&
                    (maxPrice == null || property.price <= maxPrice) &&
                    (proximityPlaces == null || property.proximityPlaces == proximityPlaces) &&
                    (status == null || property.status == status) &&
                    (minPhotos == null || property.photos.size >= minPhotos)
        }

        propertyAdapter.submitList(filteredList)
        binding.resetFilterBtn.visibility = View.VISIBLE
    }

    private fun showFormFragment() {
        formFragment = FormFragment()
        binding.propertyListHeaderAddIcon.setOnClickListener {
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack("PropertyListFragment")
                .commit()
        }
    }

    private fun showMapFragment() {
        val mapFragment = MapFragment()
        binding.propertyListHeaderMapIcon.setOnClickListener {
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .addToBackStack("PropertyListFragment")
                .commit()
        }
    }

    private fun showFilterFragment() {
        val filterFragment = FilterFragment()
        binding.showFilterFragmentBtn.setOnClickListener {
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, filterFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}