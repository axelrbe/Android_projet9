package com.openclassrooms.realestatemanager.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.adapters.PropertiesAdapter
import com.openclassrooms.realestatemanager.models.Property

class FormFragment : Fragment() {

    private lateinit var typePropertyBtn: Button
    private lateinit var pricePropertyBtn: EditText
    private lateinit var surfacePropertyBtn: EditText
    private lateinit var roomsPropertyBtn: EditText
    private lateinit var descPropertyBtn: Button
    private lateinit var descPropertyEditText: EditText
    private lateinit var addPropertyPhotosBtn: Button
    private lateinit var selectedPhotosLayout: LinearLayout
    private lateinit var addressPropertyBtn: EditText
    private lateinit var addProximityPlacesBtn: Button
    private lateinit var addProximityPlacesListView: ListView
    private lateinit var realEstateAgentPropertyBtn: EditText
    private lateinit var addPropertyBtn: Button
    private lateinit var listener: OnPropertyAddedListener

    interface OnPropertyAddedListener {
        fun onPropertyAdded(property: Property)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnPropertyAddedListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_form, container, false)

        typePropertyBtn = view.findViewById(R.id.add_property_type)
        pricePropertyBtn = view.findViewById(R.id.add_property_price)
        surfacePropertyBtn = view.findViewById(R.id.add_property_surface)
        roomsPropertyBtn = view.findViewById(R.id.add_property_rooms)
        descPropertyBtn = view.findViewById(R.id.add_property_desc)
        descPropertyEditText = view.findViewById(R.id.add_property_desc_editText)
        addPropertyPhotosBtn = view.findViewById(R.id.add_property_photos)
        selectedPhotosLayout = view.findViewById(R.id.selected_photos_layout)
        addressPropertyBtn = view.findViewById(R.id.add_property_address)
        addProximityPlacesBtn = view.findViewById(R.id.add_property_proximity)
        addProximityPlacesListView = view.findViewById(R.id.add_proximity_places_listview)
        realEstateAgentPropertyBtn = view.findViewById(R.id.add_property_realEstate_agent)
        addPropertyBtn = view.findViewById(R.id.add_property_btn)

        addPropertyPhotosBtn.setOnClickListener {
            val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getImageFromGallery.launch(pickImg)
        }

        addNewProperty()
        addPopupMenuForType()
        addEditTextForDescription()
        addPopupMenuForProximityPlaces()

        return view
    }

    private fun addNewProperty() {
        addPropertyBtn.setOnClickListener {
            val propertyType: String = typePropertyBtn.text.toString()
            val propertyPrice: Long = pricePropertyBtn.text.toString().toLong()
            val propertySurface: Long = surfacePropertyBtn.text.toString().toLong()
            val propertyRooms: Int = roomsPropertyBtn.text.toString().toInt()
            val propertyDesc: String = descPropertyBtn.text.toString()
            val propertyPhotos: List<Uri?> = selectedPhotos
            val propertyAddress: String = addressPropertyBtn.text.toString()
            val propertyRealEstateAgent: String = realEstateAgentPropertyBtn.text.toString()
            val todayDate = Utils.todayDate

            val allProximityPlaces = mutableListOf<String>()
            for (i in 0 until addProximityPlacesListView.count) {
                val proximityPlace = addProximityPlacesListView.getItemAtPosition(i) as String
                allProximityPlaces.add(proximityPlace)
            }
            val propertyProximityPlaces: List<String> = allProximityPlaces

            val newProperty = Property(propertyType, propertyPrice, propertySurface, propertyRooms, propertyDesc,
                propertyPhotos, propertyAddress, propertyProximityPlaces, getString(R.string.property_for_sale),
                todayDate, null, propertyRealEstateAgent)

            try {
                listener.onPropertyAdded(newProperty)
                typePropertyBtn.text = ""
                pricePropertyBtn.setText("")
                surfacePropertyBtn.setText("")
                roomsPropertyBtn.setText("")
                "Description".also { descPropertyBtn.text = it }
                selectedPhotosLayout.visibility = View.GONE
                addressPropertyBtn.setText("")
                realEstateAgentPropertyBtn.setText("")
                addProximityPlacesListView.visibility = View.GONE
                Toast.makeText(context, getString(R.string.success_add_property), Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, getString(R.string.failed_add_property), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPopupMenuForProximityPlaces() {
        val adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, ArrayList()) }
        addProximityPlacesListView.adapter = adapter
        val selectedItems = hashSetOf<String>()

        addProximityPlacesBtn.setOnClickListener {
            val popupMenu = context?.let { it1 -> PopupMenu(it1, addProximityPlacesBtn) }
            popupMenu?.inflate(R.menu.proximity_popup_menu)
            popupMenu?.show()

            popupMenu?.setOnMenuItemClickListener { menuItem ->
                val selectedItem = menuItem.title.toString()
                if (!selectedItems.contains(selectedItem)) {
                    selectedItems.add(selectedItem)
                    adapter?.add(selectedItem)
                    addProximityPlacesListView.visibility = View.VISIBLE
                } else {
                    val toast = Toast.makeText(context, R.string.already_selected_proximity_place, Toast.LENGTH_SHORT)
                    toast.show()
                }
                true
            }
        }
    }

    private fun addPopupMenuForType() {
        typePropertyBtn.setOnClickListener {
            val popupMenu = context?.let { it1 -> PopupMenu(it1, typePropertyBtn) }
            popupMenu?.inflate(R.menu.type_popup_menu)
            popupMenu?.show()

            popupMenu?.setOnMenuItemClickListener { menuItem ->
                typePropertyBtn.text = menuItem.title
                true
            }
        }
    }

    private fun addEditTextForDescription() {
        var isEditTextVisible = false
        descPropertyBtn.setOnClickListener {
            if (!isEditTextVisible) {
                descPropertyBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.icon_desc, 0, R.drawable.icon_dropup, 0
                )
                descPropertyEditText.visibility = View.VISIBLE
                descPropertyEditText.requestFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(descPropertyEditText, InputMethodManager.SHOW_IMPLICIT)
                isEditTextVisible = true
            } else {
                descPropertyBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.icon_desc, 0, R.drawable.icon_dropdown, 0
                )
                descPropertyEditText.visibility = View.GONE
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(descPropertyEditText.windowToken, 0)
                isEditTextVisible = false
            }
        }

        descPropertyEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                descPropertyBtn.text = s.toString()
            }
        })
    }
    private val selectedPhotos = ArrayList<Uri?>()
    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val imgUri = data?.data
                selectedPhotos.add(imgUri)
                updateSelectedPhotos()
            }
        }

    private fun updateSelectedPhotos() {
        selectedPhotosLayout.visibility = View.VISIBLE
        selectedPhotosLayout.removeAllViews()
        for (photo in selectedPhotos) {
            val imageView = ImageView(context)
            imageView.setImageURI(photo)
            imageView.layoutParams = LinearLayout.LayoutParams(200, 200)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            selectedPhotosLayout.addView(imageView)
        }
    }
}