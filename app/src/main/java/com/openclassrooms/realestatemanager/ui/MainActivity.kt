package com.openclassrooms.realestatemanager.ui

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.fragments.FormFragment
import com.openclassrooms.realestatemanager.fragments.PropertyListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerAddPropertyBtn: ImageButton
    private lateinit var propertyListFragment: PropertyListFragment
    private lateinit var formFragment: FormFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize fragments
        propertyListFragment = PropertyListFragment()
        formFragment = FormFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, propertyListFragment)
            .commit()

        // Binding the views
        headerAddPropertyBtn = binding.headerAddPropertyIcon

        // Manage the visibility of the form
        formVisibilityManagement()
    }

    private fun formVisibilityManagement() {
        var isFragmentListVisible = true

        headerAddPropertyBtn.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            isFragmentListVisible = if (isFragmentListVisible) {
                // Hide the first fragment
                fragmentTransaction.hide(propertyListFragment)
                // Show the second fragment
                fragmentTransaction.show(formFragment)
                false
            } else {
                // Hide the second fragment
                fragmentTransaction.hide(formFragment)
                // Show the first fragment
                fragmentTransaction.show(propertyListFragment)
                true
            }

            fragmentTransaction.commit()
        }
    }
}