package com.openclassrooms.realestatemanager

import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.openclassrooms.realestatemanager.Utils.convertDollarToEuro
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var addPropertyBtn: ImageButton
    private lateinit var propertiesRecyclerViewContainer: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addPropertyBtn = binding.headerAddPropertyIcon
        propertiesRecyclerViewContainer = binding.propertiesRecyclerViewContainer
        var isCardViewVisible = true

        addPropertyBtn.setOnClickListener {
            if (isCardViewVisible) {
                propertiesRecyclerViewContainer.visibility = View.GONE
                val animation = TranslateAnimation(0f, 0f, 0f, propertiesRecyclerViewContainer.height.toFloat())
                animation.duration = 500
                animation.fillAfter = true
                propertiesRecyclerViewContainer.startAnimation(animation)
                addPropertyBtn.setImageResource(R.drawable.icon_close)
                isCardViewVisible = false
            } else {
                propertiesRecyclerViewContainer.visibility = View.VISIBLE
                val animation = TranslateAnimation(0f, 0f, propertiesRecyclerViewContainer.height.toFloat(), 0f)
                animation.duration = 500
                animation.fillAfter = true
                propertiesRecyclerViewContainer.startAnimation(animation)
                addPropertyBtn.setImageResource(R.drawable.icon_add)
                isCardViewVisible = true
            }
        }
    }
}