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
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.textAlignment = View.TEXT_ALIGNMENT_CENTER

        fragmentManager = (this as AppCompatActivity).supportFragmentManager
        currencyViewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]
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
                .addToBackStack("PropertyListFragment")
                .commit()

            changeToolbarButtons()
            true
        }

        R.id.action_add_property -> {
            val formFragment = FormFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack("PropertyListFragment")
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
                        transaction.addToBackStack("PropertyListFragment")
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

    private fun resetToolbar() {
        binding.toolbar.menu.findItem(R.id.action_map).isVisible = true
        binding.toolbar.menu.findItem(R.id.action_add_property).isVisible = true
        binding.toolbar.menu.findItem(R.id.action_settings).isVisible = true
        fragmentManager.popBackStack()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


}