package com.brandonhxrr.gallery

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.brandonhxrr.gallery.adapter.ViewPagerAdapter
import com.google.gson.Gson
import java.io.File


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
        supportActionBar?.title = ""

        val bundle = intent.extras
        val path = bundle?.getString("path")
        position = bundle?.getInt("position")!!
        val gson = Gson()
        val data = intent.getStringExtra("data")
        media = gson.fromJson(data, Array<Photo>::class.java).toList()

        viewPager = findViewById(R.id.viewPager)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                fileTitle.text = File(media!![position].path).name
            }
        })

        viewPagerAdapter = ViewPagerAdapter(this, media!!)
        viewPager.adapter = viewPagerAdapter
        fileTitle.text = File(media!![position].path).name
        viewPager.currentItem = position
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}