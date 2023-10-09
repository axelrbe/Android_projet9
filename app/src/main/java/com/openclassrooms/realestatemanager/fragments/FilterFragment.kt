package com.openclassrooms.realestatemanager.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentFilterBinding


class FilterFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentFilterBinding? = null
    private val proximityPlacesSelectedItems = hashSetOf<String>()
    private lateinit var propertyListFragment: PropertyListFragment
    private lateinit var fragmentManager: FragmentManager
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager = (context as AppCompatActivity).supportFragmentManager

        propertyListFragment =
            parentFragmentManager.findFragmentById(R.id.property_list_fragment) as PropertyListFragment

        binding.applyFiltersBtn.setOnClickListener {
            applyFilters()
        }

        addPopupMenuForTypeFilter()
        addPopupMenuForProximityPlacesFilter()
        binding.filterFragmentArrowBack.setOnClickListener { fragmentManager.popBackStack() }
    }

    private fun applyFilters() {
        val type = binding.filterTypeBtn.text.toString().takeIf { it.isNotEmpty() }
        val minSurface = binding.filterSurfaceMin.text.toString().takeIf { it.isNotEmpty() }?.toInt()
        val maxSurface = binding.filterSurfaceMax.text.toString().takeIf { it.isNotEmpty() }?.toInt()
        val minPrice = binding.filterPriceMin.text.toString().takeIf { it.isNotEmpty() }?.toInt()
        val maxPrice = binding.filterPriceMax.text.toString().takeIf { it.isNotEmpty() }?.toInt()
        val proximityPlaces = getFilteredProximityPlacesItems().takeIf { it.isNotEmpty() }
        val status = binding.filterStatus.text.toString().takeIf { it.isNotEmpty() }
        val minPhotos = binding.filterPhotos.text.toString().takeIf { it.isNotEmpty() }?.toInt()

        propertyListFragment.applyFilters(
            type,
            minSurface,
            maxSurface,
            minPrice,
            maxPrice,
            proximityPlaces,
            status,
            minPhotos
        )
        fragmentManager.popBackStack()
    }

    private fun addPopupMenuForTypeFilter() {
        binding.filterTypeBtn.setOnClickListener {
            val popupMenu = context?.let { it1 -> PopupMenu(it1, binding.filterTypeBtn) }
            popupMenu?.inflate(R.menu.type_popup_menu)
            popupMenu?.show()

            popupMenu?.setOnMenuItemClickListener { menuItem ->
                binding.filterTypeBtn.text = menuItem.title
                true
            }
        }
    }

    private fun addPopupMenuForProximityPlacesFilter() {
        binding.filterProximityPlaces.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.filterProximityPlaces)
            popupMenu.inflate(R.menu.proximity_popup_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { menuItem ->
                binding.addProximityPlacesFilterScrollView.visibility = View.VISIBLE
                val selectedItem = menuItem.title.toString()
                if (!proximityPlacesSelectedItems.contains(selectedItem)) {
                    proximityPlacesSelectedItems.add(selectedItem)
                    val textView = TextView(context)
                    textView.text = selectedItem
                    textView.setPadding(0, 8, 0, 8)
                    textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.textSize = 18F
                    textView.gravity = Gravity.CENTER
                    binding.addProximityPlacesFilterLayout.addView(textView)
                    binding.addProximityPlacesFilterScrollView.post {
                        binding.addProximityPlacesFilterScrollView.fullScroll(View.FOCUS_DOWN)
                    }
                } else {
                    val toast = Toast.makeText(context, R.string.already_selected_proximity_place, Toast.LENGTH_SHORT)
                    toast.show()
                }
                true
            }
        }
    }

    private fun getFilteredProximityPlacesItems(): List<String> {
        return proximityPlacesSelectedItems.toList()
    }
}