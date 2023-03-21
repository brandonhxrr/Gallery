package com.brandonhxrr.gallery.adapter.album

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import java.io.File

class AlbumAdapter(private val glide: RequestBuilder<Bitmap>) : RecyclerView.Adapter<AlbumViewHolder>() {

    private var items = mapOf<File, List<File>>()
    //private var items = listOf<File>()
    private var indexes = listOf<File>()

    //private var items = HashMap<String, List<String>>()
    //private var indexes = listOf<String>()

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) = holder.bind(getItemAt(position), glide)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder =
        AlbumViewHolder.new(parent)

    override fun getItemCount(): Int = indexes.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(fileMap: Map<File, List<File>>) {
    //fun setItems(folders: List<File>) {
        items = fileMap
        //indexes = fileMap.keys.toList()//.sortedBy { t -> t.nameWithoutExtension }
        indexes = fileMap.keys.toList().sortedBy { it.nameWithoutExtension }
        notifyDataSetChanged()
    }

    private fun getItemAt(position: Int) = items[indexes[position]]
    //private fun getItemAt(position: Int) = items[position]

}