package com.brandonhxrr.gallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class FolderAdapter() : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var items = mapOf<File, List<File>>()
    private var indexes = listOf<File>()

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) = holder.bind(getItemAt(position))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder = FolderViewHolder.new(parent)

    override fun getItemCount(): Int = indexes.size

    fun setItems(fileMap: Map<File, List<File>>) {
        items = fileMap
        indexes = fileMap.keys.toList()
        notifyDataSetChanged()
    }

    private fun getItemAt(position: Int) = items[indexes[position]]

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        val image = view.findViewById<ImageView>(R.id.album_image)
        val title = view.findViewById<TextView>(R.id.album_title)
        val counter = view.findViewById<TextView>(R.id.album_counter)

        companion object {
            fun new(viewGroup: ViewGroup) = FolderViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.album, viewGroup, false))
        }

        fun bind(files: List<File>?) {
            (files?.isNotEmpty()).let {
                val firstChild = files!![0]
                val parent = File(firstChild.parent)
                title.text = parent.nameWithoutExtension
                image.loadImage(firstChild)
                counter.text = files.count().toString() + " items"
            }
        }

        fun ImageView.loadImage(file: File) {
            Glide.with(this)
                .load(file)
                .apply(
                    RequestOptions()
                    .placeholder(R.drawable.ic_album)
                    .centerCrop())
                .into(this)
        }
    }
}