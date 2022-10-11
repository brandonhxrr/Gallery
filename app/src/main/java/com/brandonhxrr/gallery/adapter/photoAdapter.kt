package com.brandonhxrr.gallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R

class photoAdapter(private val photoList:List<Photo>) : RecyclerView.Adapter<photoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): photoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return photoViewHolder(layoutInflater.inflate(R.layout.photo, parent, false))
    }

    override fun onBindViewHolder(holder: photoViewHolder, position: Int) {
        val item = photoList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int = photoList.size
}