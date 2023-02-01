package com.brandonhxrr.gallery

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager.widget.ViewPager
import com.brandonhxrr.gallery.adapter.ViewPagerAdapter
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class PhotoView : AppCompatActivity() {

    private var media: List<Photo>? = null
    private lateinit var viewPager: CustomViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var photoDate: TextView
    private lateinit var photoTime: TextView
    private lateinit var container: ConstraintLayout
    private lateinit var toolbar: Toolbar
    private var position: Int = 0
    private lateinit var windowInsetsController : WindowInsetsControllerCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContentView(R.layout.activity_photo_view)

        photoDate = findViewById(R.id.photo_date)
        photoTime = findViewById(R.id.photo_time)
        container = findViewById(R.id.constraintContainer)

        toolbar = findViewById(R.id.toolbar)
        val params = toolbar.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = getStatusBarHeight()
        toolbar.layoutParams = params
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
                setDateTime(position)

            }
        })

        viewPagerAdapter = ViewPagerAdapter(this, media!!)
        viewPager.adapter = viewPagerAdapter
        setDateTime(position)
        viewPager.currentItem = position
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setDateTime(position : Int) {
        val date = Date(File(media!![position].path).lastModified())
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault())
        val outputFormatDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
        val outputFormatTime = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val input = inputFormat.format(date)
        val dateParse = inputFormat.parse(input)

        photoDate.text = outputFormatDate.format(dateParse!!)
        photoTime.text = outputFormatTime.format(dateParse)
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}