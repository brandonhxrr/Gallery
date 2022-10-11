package com.brandonhxrr.gallery.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R

class photoViewHolder(view: View ) : RecyclerView.ViewHolder(view){

    val id = view.findViewById<TextView>(R.id.item_number)
    val image = view.findViewById<ImageView>(R.id.item_photo)

    fun render(photoModel: Photo) {
        id.text = photoModel.idPhoto
    }
}