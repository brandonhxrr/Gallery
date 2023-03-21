package com.brandonhxrr.gallery.adapter.album_selection

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import java.io.File

class AlbumSelectionAdapter(private val glide: RequestBuilder<Bitmap>) : RecyclerView.Adapter<AlbumSelectionViewHolder>() {

    private var items = mapOf<File, List<File>>()
    private var indexes = listOf<File>()


    override fun onBindViewHolder(holder: AlbumSelectionViewHolder, position: Int) = holder.bind(getItemAt(position), glide)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumSelectionViewHolder =
        AlbumSelectionViewHolder.new(parent)

    override fun getItemCount(): Int = indexes.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(fileMap: Map<File, List<File>>) {
        items = fileMap
        indexes = fileMap.keys.toList().sortedBy { it.nameWithoutExtension }
        notifyDataSetChanged()
    }

    private fun getItemAt(position: Int) = items[indexes[position]]

}