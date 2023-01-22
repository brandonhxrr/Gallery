package com.brandonhxrr.gallery.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R
import com.bumptech.glide.RequestBuilder

class PhotoAdapter(private val photoList:List<Photo>, private val glide: RequestBuilder<Bitmap>) : RecyclerView.Adapter<PhotoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(layoutInflater.inflate(R.layout.photo, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = photoList[position]
        holder.render(item, glide)
    }

    override fun getItemCount(): Int = photoList.size
}