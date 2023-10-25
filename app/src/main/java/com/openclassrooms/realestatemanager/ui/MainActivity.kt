package com.openclassrooms.realestatemanager.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.fragments.FormFragment
import com.openclassrooms.realestatemanager.fragments.MapFragment
import com.openclassrooms.realestatemanager.viewModel.CurrencyViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var fragmentManager: FragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentManager = (this as AppCompatActivity).supportFragmentManager

        currencyViewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        showFormFragment()
        showMapFragment()
        binding.propertyListHeaderDots.setOnClickListener {
            showPopUpMenu()
        }
    }

    private fun showFormFragment() {
        val formFragment = FormFragment()
        binding.propertyListHeaderAddIcon.setOnClickListener {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack("PropertyListFragment")
                .commit()
        }
    }

    private fun showMapFragment() {
        val mapFragment = MapFragment()
        binding.propertyListHeaderMapIcon.setOnClickListener {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .addToBackStack("PropertyListFragment")
                .commit()
        }
    }

    private fun showPopUpMenu() {
        val popupMenu = PopupMenu(this, binding.propertyListHeaderDots)
        popupMenu.menuInflater.inflate(R.menu.header_main_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_switch_currency -> {
                    currencyViewModel.toggleCurrency()
                    Toast.makeText(this, getString(R.string.convert_currency_success), Toast.LENGTH_SHORT)
                        .show()
                    true
                }

                R.id.menu_simulator -> {
                    val intent = Intent(this, SimulatorActivity::class.java)
                    this.startActivity(intent)
                    val transaction = this.supportFragmentManager.beginTransaction()
                    transaction.addToBackStack("PropertyListFragment")
                    transaction.commit()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}