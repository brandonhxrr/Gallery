package com.brandonhxrr.gallery

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.AlbumSelectionAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import java.io.File

class AlbumSelection : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var albums: HashMap<File, List<File>>
    private lateinit var albumSelectionAdapter: AlbumSelectionAdapter
    private lateinit var builder: RequestBuilder<Bitmap>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_selection)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.title = ""

        albums = albumes!!

        initRecyclerView(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initRecyclerView(context: Context) {
        recyclerView = findViewById(R.id.gridRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val glide = Glide.with(this)
        builder = glide.asBitmap()

        albumSelectionAdapter = AlbumSelectionAdapter(builder)
        albumSelectionAdapter.setItems(albums)

        recyclerView.adapter = albumSelectionAdapter
    }
}