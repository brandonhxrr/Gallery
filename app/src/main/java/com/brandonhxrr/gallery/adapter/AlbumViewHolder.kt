package com.brandonhxrr.gallery.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.R
import com.bumptech.glide.RequestBuilder
import java.io.File

class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {


    val image = view.findViewById<ImageView>(R.id.album_image)
    val title = view.findViewById<TextView>(R.id.album_title)
    val counter = view.findViewById<TextView>(R.id.album_counter)

    companion object {
        fun new(viewGroup: ViewGroup) = AlbumViewHolder(
            LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.album, viewGroup, false))
    }

    fun bind(files: List<File>?, glide: RequestBuilder<Bitmap>) {
        (files?.isNotEmpty()).let {
            val firstChild = files!![0]
            val parent = File(firstChild.parent as String)

            title.text = parent.nameWithoutExtension
            glide.load(firstChild).centerCrop().into(image)

            val items = parent.listFiles()?.size!!

            counter.text = if (items > 1)  parent.listFiles()?.size.toString() + " items" else parent.listFiles()?.size.toString() + " item"
        }
    }
}