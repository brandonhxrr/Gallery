package com.brandonhxrr.gallery.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import java.io.File
import java.util.Comparator

class AlbumAdapter(val glide: RequestBuilder<Bitmap>) : RecyclerView.Adapter<AlbumViewHolder>() {

    private var items = mapOf<File, List<File>>()
    private var indexes = listOf<File>()

    //private var items = HashMap<String, List<String>>()
    //private var indexes = listOf<String>()

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) = holder.bind(getItemAt(position), glide)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder = AlbumViewHolder.new(parent)

    override fun getItemCount(): Int = indexes.size

    fun setItems(fileMap: Map<File, List<File>>) {
        items = fileMap
        indexes = fileMap.keys.sortedBy { t -> t.nameWithoutExtension }.toList()
        notifyDataSetChanged()
    }

    /*fun setAlbums(albums: HashMap<String, List<String>>) {
        items = albums
        indexes = albums.keys.toList()
    }*/

    private fun getItemAt(position: Int) = items[indexes[position]]

}