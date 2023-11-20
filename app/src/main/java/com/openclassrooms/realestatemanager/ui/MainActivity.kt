package com.openclassrooms.realestatemanager.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.application.RealEstateApplication
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.fragments.FormFragment
import com.openclassrooms.realestatemanager.fragments.MapFragment
import com.openclassrooms.realestatemanager.fragments.PropertyListFragment
import com.openclassrooms.realestatemanager.repositories.PropertyRepository
import com.openclassrooms.realestatemanager.viewModel.CurrencyViewModel
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModel
import com.openclassrooms.realestatemanager.viewModel.PropertyViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var fragmentManager: FragmentManager
    private lateinit var propertyViewModel: PropertyViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var propertyDao: PropertyDao
    private lateinit var formFragment: FormFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.textAlignment = View.TEXT_ALIGNMENT_CENTER

        formFragment = FormFragment()
        fragmentManager = (this as AppCompatActivity).supportFragmentManager
        currencyViewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        // No property alert management
        binding.noPropertyAddBtn?.setOnClickListener {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack(null)
                .commit()
        }

        propertyDao = RealEstateApplication.getInstance(this).propertyDao()
        propertyRepository = PropertyRepository(propertyDao)
        propertyViewModel = ViewModelProvider(
            this,
            PropertyViewModelFactory(propertyRepository, this.application)
        )[PropertyViewModel::class.java]
        lifecycleScope.launch {
            propertyViewModel.getPropertyList().collect { properties ->
                if (properties.isEmpty()) {
                    binding.noPropertyAlertContainer?.visibility = View.VISIBLE
                } else {
                    binding.noPropertyAlertContainer?.visibility = View.GONE
                }
            }
        }

        // Change language depends on the device
        val currentLocale = Locale.getDefault().language
        val config = resources.configuration
        val newLocale = if (currentLocale == "en") Locale.ENGLISH else Locale.FRENCH
        val newContext = this.createConfigurationContext(config)
        config.setLocale(newLocale)
        newContext.resources.updateConfiguration(config, newContext.resources.displayMetrics)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_map -> {
            val mapFragment = MapFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .addToBackStack(null)
                .commit()

            changeToolbarButtons()
            true
        }

        R.id.action_add_property -> {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack(null)
                .commit()

            changeToolbarButtons()
            true
        }

        R.id.action_settings -> {
            val popupMenu = PopupMenu(this, findViewById(R.id.action_settings))
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
                        transaction.addToBackStack(null)
                        transaction.commit()
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
            true
        }

        android.R.id.home -> {
            val tabletSize = resources.getBoolean(R.bool.isTablet)
            if(!tabletSize) {
                val propertyListFragment = PropertyListFragment()
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, propertyListFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            resetToolbar()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun changeToolbarButtons() {
        binding.toolbar.menu.findItem(R.id.action_map).isVisible = false
        binding.toolbar.menu.findItem(R.id.action_add_property).isVisible = false
        binding.toolbar.menu.findItem(R.id.action_settings).isVisible = false
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun resetToolbar() {
        binding.toolbar.menu.findItem(R.id.action_map).isVisible = true
        binding.toolbar.menu.findItem(R.id.action_add_property).isVisible = true
        binding.toolbar.menu.findItem(R.id.action_settings).isVisible = true
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


}