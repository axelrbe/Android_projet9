package com.openclassrooms.realestatemanager.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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
import com.google.android.material.chip.Chip
import com.openclassrooms.realestatemanager.BuildConfig
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.customView.NamedImageView
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.databinding.FragmentFormBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.ui.MainActivity
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
    private lateinit var getImageFromGallery: ActivityResultLauncher<Intent>
    private lateinit var getImageFromCamera: ActivityResultLauncher<Intent>
    private val selectedPhotos = ArrayList<Property.Photo>()

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

        getImageFromGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val imgUri = data?.data
                val alertDialog = createNameDialog(imgUri)
                alertDialog.show()
            }
        }

        getImageFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val bitmap = data?.extras?.get("data") as Bitmap?
                val imgUri = saveBitmapToFile(bitmap)
                val alertDialog = createNameDialog(imgUri)
                alertDialog.show()
            }
        }

        showDialogForPhotos()
        addNewProperty()
        addPopupMenuForType()
        addEditTextForDescription()
        addPopupMenuForProximityPlaces()
        autoCompleteFragmentManagement()
    }

    private fun validateFormFields(): Boolean {
        val propertyType = binding.addPropertyType.text.toString()
        val propertyPrice = binding.addPropertyPrice.text.toString().toLongOrNull()
        val propertySurface = binding.addPropertySurface.text.toString().toLongOrNull()
        val propertyRooms = binding.addPropertyRooms.text.toString().toIntOrNull()
        val propertyDesc = binding.addPropertyDesc.text.toString()
        val propertyRealEstateAgent = binding.addPropertyRealEstateAgent.text.toString()
        val todayDate = Utils.todayDate

        return (
                propertyType.isNotEmpty() &&
                        propertyPrice != null &&
                        propertySurface != null &&
                        propertyRooms != null &&
                        propertyDesc.isNotEmpty() &&
                        propertyRealEstateAgent.isNotEmpty() &&
                        todayDate.isNotEmpty()
                )
    }

    private fun addNewProperty() {
        binding.addPropertyBtn.setOnClickListener {
            if (validateFormFields()) {
                val propertyType = binding.addPropertyType.text.toString()
                val propertyPrice = binding.addPropertyPrice.text.toString().toLong()
                val propertySurface = binding.addPropertySurface.text.toString().toLong()
                val propertyRooms = binding.addPropertyRooms.text.toString().toInt()
                val propertyDesc = binding.addPropertyDesc.text.toString()
                if (selectedPhotos.isEmpty()) {
                    val drawableUri =
                        Uri.parse("android.resource://com.openclassrooms.realestatemanager/drawable/no_images_found")
                    val drawableUriString: String = drawableUri.toString()
                    Log.d("photoUri", "addNewProperty: $drawableUri")
                    val noPhoto = Property.Photo(uri = drawableUriString, name = "")
                    selectedPhotos.add(noPhoto)
                }
                val propertyPhotos = selectedPhotos
                val propertyRealEstateAgent = binding.addPropertyRealEstateAgent.text.toString()
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
                    status = getString(R.string.for_sale),
                    entryDate = todayDate,
                    soldDate = "",
                    realEstateAgent = propertyRealEstateAgent
                )

                try {
                    propertyViewModel.addProperty(newProperty)
                    (activity as MainActivity).resetToolbar()
                    binding.addPropertyType.text = ContextCompat.getString(requireContext(), R.string.add_property_type)
                    binding.addPropertyPrice.setText("")
                    binding.addPropertySurface.setText("")
                    binding.addPropertyRooms.setText("")
                    binding.addPropertyDescEditText.setText("")
                    binding.selectedPhotosLayout.visibility = View.GONE
                    proximityPlacesSelectedItems.clear()
                    selectedPhotos.clear()
                    binding.addPropertyRealEstateAgent.setText("")

                    Toast.makeText(context, getString(R.string.success_add_property), Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(context, getString(R.string.failed_add_property), Toast.LENGTH_SHORT).show()
                }
                fragmentManager.popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.empty_fields_error_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addPopupMenuForProximityPlaces() {
        val tabletSize = resources.getBoolean(R.bool.isTablet)
        binding.addPropertyProximity.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.addPropertyProximity)
            popupMenu.inflate(R.menu.proximity_popup_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val selectedItem = menuItem.title.toString()
                val chipGroup = binding.formProximityPlacesChipGroup
                if (!proximityPlacesSelectedItems.contains(selectedItem)) {
                    proximityPlacesSelectedItems.add(selectedItem)
                    val chip = Chip(requireContext())
                    chip.text = selectedItem
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
                    chip.setChipBackgroundColorResource(R.color.white)
                    chip.setTypeface(null, Typeface.BOLD)
                    if (tabletSize) {
                        chip.textSize = 20F
                    } else {
                        chip.textSize = 18F
                    }
                    chipGroup.addView(chip)
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

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            val packageManager = context?.packageManager
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (packageManager?.let { it1 -> takePicture.resolveActivity(it1) } != null) {
                getImageFromCamera.launch(takePicture)
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    // Permission request launcher for storage
    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            val pickImg = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImageFromGallery.launch(pickImg)
        } else {
            Toast.makeText(requireContext(), getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun showDialogForPhotos() {
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
                        // Check camera permission before launching the camera
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val packageManager = context?.packageManager
                            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            if (packageManager?.let { it1 -> takePicture.resolveActivity(it1) } != null) {
                                getImageFromCamera.launch(takePicture)
                            }
                        } else {
                            // Request camera permission
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    options[item] == getString(R.string.photos_choice_gallery) -> {
                        // Check storage permission before launching the gallery
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                            val pickImg = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            getImageFromGallery.launch(pickImg)
                        } else {
                            // Request storage permission
                            requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    }

                    options[item] == getString(R.string.cancel) -> {
                        dialog.dismiss()
                    }
                }
            }
            builder.show()
        }
    }
    
    private fun createNameDialog(imgUri: Uri?): AlertDialog {
        val nameEditText = EditText(context)
        nameEditText.hint = getString(R.string.photo_name_editText)
        nameEditText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.name_photo)
            .setView(nameEditText)
            .setPositiveButton(getString(R.string.validate)) { dialog, _ ->
                val name = nameEditText.text.toString()
                val photo = Property.Photo(uri = imgUri.toString(), name = name)
                selectedPhotos.add(photo)
                updateSelectedPhotos()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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
            namedImageView?.layoutParams = LinearLayout.LayoutParams(
                300, 300
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

    private fun autoCompleteFragmentManagement() {
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setTypesFilter(listOf(PlaceTypes.ADDRESS))

        val editText = autocompleteFragment.view?.findViewById<EditText>(R.id.places_autocomplete_search_input)
        val imageView = autocompleteFragment.view?.findViewById<ImageView>(R.id.places_autocomplete_search_button)
        val imageViewCloseBtn =
            autocompleteFragment.view?.findViewById<ImageView>(R.id.places_autocomplete_clear_button)

        editText?.hint = ContextCompat.getString(requireContext(), R.string.add_property_address)
        editText?.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.grayedText))
        editText?.setTextColor(Color.WHITE)
        editText?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_border)
        editText?.gravity = Gravity.CENTER
        editText?.textSize = 18F
        editText?.setTypeface(null, Typeface.BOLD)
        imageView?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_address))
        imageView?.setColorFilter(Color.WHITE)
        imageView?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_border)
        imageViewCloseBtn?.setColorFilter(Color.WHITE)

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
}