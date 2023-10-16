package com.openclassrooms.realestatemanager.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.openclassrooms.realestatemanager.BuildConfig
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.customView.NamedImageView
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.databinding.FragmentFormBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.utils.Utils
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModel
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModelFactory
import java.io.File
import java.io.FileOutputStream

class FormFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentFormBinding? = null
    private lateinit var propertyDao: PropertyDao
    private lateinit var propertyViewModel: PropertyViewModel
    private lateinit var propertyRepository: PropertyRepository
    private val proximityPlacesSelectedItems = hashSetOf<String>()
    private lateinit var placesClient: PlacesClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var fragmentManager: FragmentManager
    private var selectedAddress: String? = null
    private var selectedAddressLatLng: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager = (context as AppCompatActivity).supportFragmentManager
        propertyDao = RealEstateApplication.getInstance(requireContext()).propertyDao()
        propertyRepository = PropertyRepository(propertyDao)
        propertyViewModel = ViewModelProvider(
            requireActivity(),
            PropertyViewModelFactory(propertyRepository, requireActivity().application)
        )[PropertyViewModel::class.java]

        binding.addPropertyPhotos.setOnClickListener {
            val options = arrayOf<CharSequence>(
                getString(R.string.photos_choice_camera),
                getString(R.string.photos_choice_gallery), getString(R.string.cancel)
            )
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.add_photos_title))
            builder.setItems(options) { dialog, item ->
                when {
                    options[item] == getString(R.string.photos_choice_camera) -> {
                        val packageManager = context?.packageManager
                        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (packageManager?.let { it1 -> takePicture.resolveActivity(it1) } != null) {
                            getImageFromCamera.launch(takePicture)
                        }
                    }

                    options[item] == getString(R.string.photos_choice_gallery) -> {
                        val pickImg = Intent(Intent.ACTION_GET_CONTENT)
                        pickImg.type = "image/*"
                        getImageFromGallery.launch(pickImg)
                    }

                    options[item] == getString(R.string.cancel) -> {
                        dialog.dismiss()
                    }
                }
            }
            builder.show()
        }

        binding.formHeaderClose.setOnClickListener {
            fragmentManager.popBackStack()
        }

        addNewProperty()
        addPopupMenuForType()
        addEditTextForDescription()
        addPopupMenuForProximityPlaces()
        autoCompleteFragmentManagement()
    }

    private fun autoCompleteFragmentManagement() {
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setTypesFilter(listOf(PlaceTypes.ADDRESS))

        val editText = autocompleteFragment.view?.findViewById<EditText>(R.id.places_autocomplete_search_input)
        val imageView = autocompleteFragment.view?.findViewById<ImageView>(R.id.places_autocomplete_search_button)

        editText?.hint = ContextCompat.getString(requireContext(), R.string.add_property_address)
        editText?.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.grayedText))
        editText?.setTextColor(Color.WHITE)
        editText?.gravity = Gravity.CENTER
        editText?.textSize = 18F
        editText?.setTypeface(null, Typeface.BOLD)
        imageView?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_address))
        imageView?.setColorFilter(Color.WHITE)

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                selectedAddress = place.name
                selectedAddressLatLng = place.latLng

                // Add a marker to the map and move the camera to the selected place
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(selectedAddressLatLng!!).title(selectedAddress))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedAddressLatLng!!, 15f))

                Log.i("GettingPlacesInfo", "Place: ${place.name}, ${place.id}, ${place.latLng}")
            }

            override fun onError(status: Status) {
                Log.i("GettingPlacesInfo", "An error occurred: $status")
            }
        })

        mapFragment = childFragmentManager.findFragmentById(R.id.confirmation_map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
        }
    }

    private fun addNewProperty() {
        binding.addPropertyBtn.setOnClickListener {
            val propertyType: String = binding.addPropertyType.text.toString()
            val propertyPrice: Long = binding.addPropertyPrice.text.toString().toLong()
            val propertySurface: Long = binding.addPropertySurface.text.toString().toLong()
            val propertyRooms: Int = binding.addPropertyRooms.text.toString().toInt()
            val propertyDesc: String = binding.addPropertyDesc.text.toString()
            val propertyPhotos: List<Property.Photo> = selectedPhotos
            val propertyRealEstateAgent: String = binding.addPropertyRealEstateAgent.text.toString()
            val todayDate = Utils.todayDate

            val newProperty = Property(
                type = propertyType,
                price = propertyPrice,
                surface = propertySurface,
                rooms = propertyRooms,
                desc = propertyDesc,
                photos = propertyPhotos,
                address = selectedAddress.toString(),
                location = selectedAddressLatLng,
                proximityPlaces = getPopupMenuSelectedItems(),
                status = getString(R.string.property_for_sale),
                entryDate = todayDate,
                soldDate = null,
                realEstateAgent = propertyRealEstateAgent
            )

            try {
                Log.i("addressLatLng", "addNewProperty: $selectedAddressLatLng")
                propertyViewModel.addProperty(newProperty)
                "Type".also { binding.addPropertyType.text = it }
                binding.addPropertyPrice.setText("")
                binding.addPropertySurface.setText("")
                binding.addPropertyRooms.setText("")
                "Description".also { binding.addPropertyDesc.text = it }
                binding.selectedPhotosLayout.visibility = View.GONE
                binding.addPropertyRealEstateAgent.setText("")
                binding.addProximityPlacesLayout.visibility = View.GONE
                Toast.makeText(context, getString(R.string.success_add_property), Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, getString(R.string.failed_add_property), Toast.LENGTH_SHORT).show()
            }
            fragmentManager.popBackStack()
        }
    }

    private fun addPopupMenuForProximityPlaces() {
        binding.addPropertyProximity.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.addPropertyProximity)
            popupMenu.inflate(R.menu.proximity_popup_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { menuItem ->
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
                    binding.addProximityPlacesLayout.addView(textView)
                    binding.addProximityPlacesScrollView.post {
                        binding.addProximityPlacesScrollView.fullScroll(View.FOCUS_DOWN)
                    }
                } else {
                    val toast = Toast.makeText(context, R.string.already_selected_proximity_place, Toast.LENGTH_SHORT)
                    toast.show()
                }
                true
            }
        }
    }

    private fun getPopupMenuSelectedItems(): List<String> {
        return proximityPlacesSelectedItems.toList()
    }

    private fun addPopupMenuForType() {
        binding.addPropertyType.setOnClickListener {
            val popupMenu = context?.let { it1 -> PopupMenu(it1, binding.addPropertyType) }
            popupMenu?.inflate(R.menu.type_popup_menu)
            popupMenu?.show()

            popupMenu?.setOnMenuItemClickListener { menuItem ->
                binding.addPropertyType.text = menuItem.title
                true
            }
        }
    }

    private fun addEditTextForDescription() {
        var isEditTextVisible = false
        binding.addPropertyDesc.setOnClickListener {
            if (!isEditTextVisible) {
                binding.addPropertyDesc.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.icon_desc, 0, R.drawable.icon_small_drop_up, 0
                )
                binding.addPropertyDescEditText.visibility = View.VISIBLE
                binding.addPropertyDescEditText.requestFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.addPropertyDescEditText, InputMethodManager.SHOW_IMPLICIT)
                isEditTextVisible = true
            } else {
                binding.addPropertyDesc.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.icon_desc, 0, R.drawable.icon_dropdown, 0
                )
                binding.addPropertyDescEditText.visibility = View.GONE
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.addPropertyDescEditText.windowToken, 0)
                isEditTextVisible = false
            }
        }

        binding.addPropertyDescEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.addPropertyDesc.text = s.toString()
            }
        })
    }

    private val selectedPhotos = ArrayList<Property.Photo>()
    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val imgUri = data?.data
                val alertDialog = createNameDialog(imgUri)
                alertDialog.show()
            }
        }

    private val getImageFromCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val bitmap = data?.extras?.get("data") as Bitmap?
                val imgUri = saveBitmapToFile(bitmap)
                val alertDialog = createNameDialog(imgUri)
                alertDialog.show()
            }
        }

    private fun createNameDialog(imgUri: Uri?): AlertDialog {
        val nameEditText = EditText(context)
        nameEditText.hint = "Enter photo name"
        nameEditText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        return AlertDialog.Builder(requireContext())
            .setTitle("Enter photo name")
            .setView(nameEditText)
            .setPositiveButton("OK") { dialog, _ ->
                val name = nameEditText.text.toString()
                val photo = Property.Photo(uri = imgUri.toString(), name = name)
                selectedPhotos.add(photo)
                updateSelectedPhotos()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun updateSelectedPhotos() {
        binding.selectedPhotosLayout.visibility = View.VISIBLE
        binding.selectedPhotosLayout.removeAllViews()
        for (photo in selectedPhotos) {
            val namedImageView = context?.let { NamedImageView(it, null) }
            namedImageView?.setImageUri(Uri.parse(photo.uri))
            namedImageView?.setText(photo.name)
            namedImageView?.layoutParams = LinearLayout.LayoutParams(300, 300
            ).apply {
                setMargins(0, 0, 20, 0)
            }
            binding.selectedPhotosLayout.addView(namedImageView)

            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.photo_name))
                .setView(namedImageView?.getNameTextView())
                .setPositiveButton(getString(R.string.add)) { dialog, _ ->
                    val name = namedImageView?.getNameTextView()?.text.toString()
                    photo.name = name
                    namedImageView?.setText(name)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            namedImageView?.setOnClickListener {
                alertDialog.show()
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap?): Uri {
        val file = File(context?.cacheDir, "image.jpg")
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return FileProvider.getUriForFile(
            requireContext(),
            "${context?.packageName}.provider",
            file
        )
    }
}