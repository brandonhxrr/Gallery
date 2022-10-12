package com.brandonhxrr.gallery.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Album
import com.brandonhxrr.gallery.R
import com.bumptech.glide.Glide

class albumViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val image = view.findViewById<ImageView>(R.id.album_image)
    val title = view.findViewById<TextView>(R.id.album_title)
    val counter = view.findViewById<TextView>(R.id.album_counter)

    fun render(albumModel : Album) {
        Glide.with(image.context).load(albumModel.imagePath).centerCrop().placeholder(R.drawable.ic_album).into(image)
        title.text = albumModel.folderNames
        counter.text = albumModel.imgCount.toString() + " items"
    }
}