package com.openclassrooms.realestatemanager.customView

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.openclassrooms.realestatemanager.R

class NamedImageView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val imageView: ImageView
    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_named_imageview, this, true)
        imageView = findViewById(R.id.custom_view_image_view)
        textView = findViewById(R.id.custom_view_text_view)
    }

    fun setImageUri(uri: Uri) {
        Glide.with(context)
            .load(uri)
            .centerCrop()
            .into(imageView)
    }

    fun setText(text: String) {
        textView.text = text
    }

    fun getNameTextView(): TextView {
        return textView
    }
}