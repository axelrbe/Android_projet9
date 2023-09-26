package com.openclassrooms.realestatemanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.adapters.PropertiesAdapter
import com.openclassrooms.realestatemanager.models.Property

class PropertyListFragment : Fragment(), FormFragment.OnPropertyAddedListener {

    private val propertyList = mutableListOf<Property>()
    private lateinit var propertyAdapter: PropertiesAdapter
    private lateinit var propertyRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_property_list, container, false)

        // Recycler view adapter
        propertyRecyclerView = view.findViewById(R.id.properties_recycler_view)
        propertyAdapter = PropertiesAdapter(propertyList)
        propertyRecyclerView.layoutManager = LinearLayoutManager(context)
        propertyRecyclerView.adapter = propertyAdapter
        return view
    }

    override fun onPropertyAdded(property: Property) {
        propertyList.add(property)
        propertyAdapter.notifyItemInserted(propertyList.size - 1)
    }
}