package com.brandonhxrr.gallery.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R
import com.bumptech.glide.RequestBuilder

class PhotoAdapter(
    photoList: List<Photo>,
    private val glide: RequestBuilder<Bitmap>,
    private val layout: Int
) : RecyclerView.Adapter<PhotoViewHolder>() {

    var dataList : List<Photo> = photoList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(layoutInflater.inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = dataList[position]
        holder.render(item, glide)
    }

    override fun getItemCount(): Int = dataList.size
}