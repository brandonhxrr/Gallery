package com.brandonhxrr.gallery

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.brandonhxrr.gallery.adapter.ViewPagerAdapter
import com.google.gson.Gson


class PhotoView : AppCompatActivity() {

    private var media: List<Photo>? = null
    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var fileTitle: TextView
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        fileTitle = findViewById(R.id.fileTitle)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        val bundle = intent.extras
        val path = bundle?.getString("path")
        position = bundle?.getInt("position")!!
        val gson = Gson()
        val data = intent.getStringExtra("data")
        media = gson.fromJson(data, Array<Photo>::class.java).toList()

        viewPager = findViewById(R.id.viewPager)

        viewPagerAdapter = ViewPagerAdapter(this, media!!, fileTitle)
        viewPager.adapter = viewPagerAdapter
        viewPager.currentItem = position
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}