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
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.adapters.PropertyAdapter
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.databinding.FragmentPropertyListBinding
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModel
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PropertyListFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentPropertyListBinding? = null
    private lateinit var propertyAdapter: PropertyAdapter
    private lateinit var propertyRecyclerView: RecyclerView
    private lateinit var propertyDao: PropertyDao
    private lateinit var propertyViewModel: PropertyViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var formFragment: FormFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPropertyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Recycler view adapter
        propertyDao = RealEstateApplication.getInstance(requireContext()).propertyDao()
        propertyRecyclerView = view.findViewById(R.id.properties_recycler_view)
        propertyAdapter = PropertyAdapter()
        propertyRecyclerView.layoutManager = LinearLayoutManager(context)
        propertyRecyclerView.adapter = propertyAdapter

        headerBtnManagement()
        showPropertyList()
    }

    private fun headerBtnManagement() {
        // TODO search through the property list with search icon
        formFragment = FormFragment()
        binding.propertyListHeaderAddIcon.setOnClickListener {
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .commit()
        }
    }

    private fun showPropertyList() {
        // Observe the propertyList and update the UI
        propertyDao = RealEstateApplication.getInstance(requireContext()).propertyDao()
        propertyRepository = PropertyRepository(propertyDao)
        propertyViewModel = ViewModelProvider(this, PropertyViewModelFactory(propertyRepository, requireActivity().application))[PropertyViewModel::class.java]
        propertyViewModel.getPropertyList().onEach { properties ->
            propertyAdapter.submitList(properties)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}