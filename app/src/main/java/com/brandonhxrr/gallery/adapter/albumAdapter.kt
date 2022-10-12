package com.brandonhxrr.gallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Album
import com.brandonhxrr.gallery.R

class albumAdapter(private val albumList:List<Album>) : RecyclerView.Adapter<albumViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): albumViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return albumViewHolder(layoutInflater.inflate(R.layout.album, parent, false))
    }

    override fun onBindViewHolder(holder: albumViewHolder, position: Int) {
        val item = albumList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int = albumList.size
}