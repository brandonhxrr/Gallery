package com.brandonhxrr.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide

class PhotoView : AppCompatActivity() {
    private lateinit var image: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        image = findViewById(R.id.image)

        val bundle = intent.extras
        val path = bundle?.getString("path")

        Glide.with(this).load(path).into(image)
    }
}