package com.openclassrooms.realestatemanager.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.databinding.FragmentDetailsBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.utils.Utils
import com.openclassrooms.realestatemanager.viewModel.CurrencyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailsBinding? = null
    private var isEditable: Boolean = false
    private lateinit var propertyListFragment: PropertyListFragment
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var property: Property
    private lateinit var propertyDao: PropertyDao
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var proximityPlaces: List<String>
    private lateinit var chipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        property = arguments?.getParcelable("property")!!

        propertyDao = RealEstateApplication.getInstance(requireContext()).propertyDao()
        propertyRepository = PropertyRepository(propertyDao)
        currencyViewModel = ViewModelProvider(requireActivity())[CurrencyViewModel::class.java]

        binding.arrowBackBtn?.setOnClickListener {
            propertyListFragment = PropertyListFragment()
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.popBackStack()
        }

        binding.showAllEditIconBtn.setOnClickListener {
            isEditable = !isEditable
            updateDrawablesVisibility()
        }

        binding.propertySoldBtn.setOnClickListener {
            setTheSoldStatusAndDate()
        }

        val propertySold = ContextCompat.getString(requireContext(), R.string.sold)
        if (property.status == propertySold) {
            (ContextCompat.getString(requireContext(), R.string.sold).uppercase() + " since " + property.soldDate).also { binding.propertySoldBtn.text = it }
            binding.propertySoldBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
            binding.propertySoldBtn.setBackgroundColor(Color.TRANSPARENT)
            binding.propertySoldBtn.isClickable = false
            binding.propertySoldBtn.setTypeface(null, Typeface.BOLD)
        }

        loadPropertyData()
        launchModification()
    }

    private fun loadPropertyData() {
        binding.detailsPropertyType?.text = property.type
        "${property.surface}m²".also { binding.detailsPropertySurface.text = it }
        binding.detailsPropertyRooms.text = property.rooms.toString()
        binding.detailsPropertyDesc.text = property.desc
        binding.detailsPropertyEntryDate.text = property.entryDate
        binding.detailsPropertyAddress.text = property.address
        binding.detailsPropertyRealEstateAgent.text = property.realEstateAgent

        currencyViewModel.isEuro.observe(viewLifecycleOwner) { isEuro ->
            val price = if (isEuro) {
                Utils.convertDollarToEuro(property.price).toString() + "€"
            } else {
                "$" + property.price.toString()
            }
            binding.detailsPropertyPrice.text = price
        }


        // Add the proximity places to the chipGroup
        proximityPlaces = property.proximityPlaces
        chipGroup = binding.detailsProximityPlacesChipGroup
        chipGroup.removeAllViews()
        for (place in proximityPlaces) {
            val chip = Chip(requireContext())
            chip.text = place
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTypeface(null, Typeface.BOLD)
            chip.textSize = 18F
            chipGroup.addView(chip)
        }

        showCustomViewForImage(property)

        // Google map implementation
        mapFragment = childFragmentManager.findFragmentById(R.id.confirmation_map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.clear()
            property.location?.let {
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

    private fun updateDrawablesVisibility() {
        val purpleDrawableResId = R.drawable.icon_modify_small_purple
        val whiteDrawableResId = R.drawable.icon_modify_small_white

        binding.detailsPropertyDescEditBtn.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (if (isEditable) purpleDrawableResId else null)?.let { ContextCompat.getDrawable(requireContext(), it) }, null)
        binding.detailsPropertySurfaceEditBtn.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (if (isEditable) whiteDrawableResId else null)?.let { ContextCompat.getDrawable(requireContext(), it) }, null)
        binding.detailsPropertyRoomsEditBtn.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (if (isEditable) whiteDrawableResId else null)?.let { ContextCompat.getDrawable(requireContext(), it) }, null)
        binding.detailsPropertyProximityPlacesEditBtn.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (if (isEditable) whiteDrawableResId else null)?.let { ContextCompat.getDrawable(requireContext(), it) }, null)
        binding.detailsPropertyPriceEditBtn.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (if (isEditable) whiteDrawableResId else null)?.let { ContextCompat.getDrawable(requireContext(), it) }, null)
        binding.detailsPropertyRealEstateAgentEditBtn.setCompoundDrawablesWithIntrinsicBounds(null, null,
            (if (isEditable) whiteDrawableResId else null)?.let { ContextCompat.getDrawable(requireContext(), it) }, null)
    }

    private fun launchModification() {
        binding.detailsPropertyDescEditBtn.setOnClickListener {
            modifyPropertyData(binding.detailsPropertyDesc, binding.saveDescEditBtn, "Desc")
            binding.saveDescEditBtn.visibility = View.VISIBLE
        }
        binding.detailsPropertyRoomsEditBtn.setOnClickListener {
            modifyPropertyData(binding.detailsPropertyRooms, binding.saveRoomsEditBtn, "Rooms")
            binding.saveRoomsEditBtn.visibility = View.VISIBLE
        }
        binding.detailsPropertyPriceEditBtn.setOnClickListener {
            modifyPropertyData(binding.detailsPropertyPrice, binding.savePriceEditBtn, "Price")
            binding.savePriceEditBtn.visibility = View.VISIBLE
        }
        binding.detailsPropertyRealEstateAgentEditBtn.setOnClickListener {
            modifyPropertyData(binding.detailsPropertyRealEstateAgent, binding.saveRealEstateAgentEditBtn, "RealEstateAgent")
            binding.saveRealEstateAgentEditBtn.visibility = View.VISIBLE
        }
        binding.detailsPropertySurfaceEditBtn.setOnClickListener {
            modifyPropertyData(binding.detailsPropertySurface, binding.saveSurfaceEditBtn, "Surface")
            binding.saveSurfaceEditBtn.visibility = View.VISIBLE
        }
        binding.detailsPropertyProximityPlacesEditBtn.setOnClickListener {
            modifyListOfProximityPlaces(binding.addProximityPlaceEditText, binding.saveProximityPlacesEditBtn, binding.addNewProximityPlaceBtn)
            binding.addNewProximityPlaceContainer.visibility = View.VISIBLE
            binding.saveProximityPlacesEditBtn.visibility = View.VISIBLE
        }
    }

    private fun modifyListOfProximityPlaces(
        editText: EditText,
        saveBtn: ImageButton,
        addBtn: ImageButton
    ) {
        val updatedListOfProximityPlaces: MutableList<String> = mutableListOf()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            updatedListOfProximityPlaces.add(chip.text.toString().trim())
        }

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(chip)
                updatedListOfProximityPlaces.remove(chip.text.toString().trim())
            }
        }

        addBtn.setOnClickListener{
            val chipText = editText.text.toString().trim()
            if (chipText.isNotEmpty()) {
                val chip = Chip(context)
                chip.text = chipText
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
                chip.setChipBackgroundColorResource(R.color.white)
                chip.setTypeface(null, Typeface.BOLD)
                chip.textSize = 18F
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    chipGroup.removeView(chip)
                }
                chipGroup.addView(chip)
                updatedListOfProximityPlaces.add(chipText)
                editText.text.clear()
            }
        }

        saveBtn.setOnClickListener{
            saveBtn.visibility = View.GONE
            binding.addNewProximityPlaceContainer.visibility = View.GONE
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                chip.isCloseIconVisible = false
            }

            CoroutineScope(Dispatchers.IO).launch {
                val updatedProperty = propertyDao.getPropertyById(property.id)
                if (updatedProperty != null) {
                    Log.d("proximityPlaces", "modifyListOfProximityPlaces: $updatedListOfProximityPlaces")
                    updatedProperty.proximityPlaces = updatedListOfProximityPlaces.toList()
                    propertyDao.updateProperty(updatedProperty)
                }
            }
        }
    }

    private fun modifyPropertyData(textView: TextView, saveBtn: ImageButton, dataToChange: String) {
        val originalText = textView.text.toString()
        val parent = textView.parent as ViewGroup
        val index = parent.indexOfChild(textView)
        parent.removeView(textView)

        val newEditText = EditText(context)
        newEditText.setText(originalText)
        newEditText.gravity = Gravity.CENTER
        if (dataToChange == "Desc") {
            newEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryPurple))
        } else {
            newEditText.setTextColor(Color.WHITE)
        }
        newEditText.textSize = 14F
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        newEditText.layoutParams = layoutParams
        parent.addView(newEditText, index)

        saveBtn.setOnClickListener {
            saveBtn.visibility = View.GONE

            val modifiedText = newEditText.text.toString()
            parent.removeView(newEditText)
            textView.text = modifiedText
            parent.addView(textView, index)

            // Update the property description in the Room database
            CoroutineScope(Dispatchers.IO).launch {
                val updatedProperty = propertyDao.getPropertyById(property.id)
                val numberOnly = modifiedText.replace(Regex("[^0-9]"), "")
                if (updatedProperty != null) {
                    when (dataToChange) {
                        "RealEstateAgent" -> updatedProperty.realEstateAgent = modifiedText
                        "Desc" -> updatedProperty.desc = modifiedText
                        "Price" -> updatedProperty.price = numberOnly.toLong()
                        "Surface" -> updatedProperty.surface = numberOnly.toLong()
                        "Rooms" -> updatedProperty.rooms = modifiedText.toInt()
                    }

                    propertyDao.updateProperty(updatedProperty)
                }
            }
        }
    }

    private fun setTheSoldStatusAndDate() {
        binding.propertySoldDate.visibility = View.VISIBLE
        binding.propertySoldDateValidation.visibility = View.VISIBLE

        binding.propertySoldDateValidation.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val updatedProperty = propertyDao.getPropertyById(property.id)
                val soldDate = binding.propertySoldDate.text

                if (updatedProperty != null) {
                    updatedProperty.soldDate = soldDate.toString()
                    updatedProperty.status = ContextCompat.getString(requireContext(), R.string.sold)
                    propertyDao.updateProperty(updatedProperty)
                }
            }

            binding.propertySoldDate.visibility = View.GONE
            binding.propertySoldDateValidation.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(property: Property) =
            DetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("property", property)
                }
            }
    }
}