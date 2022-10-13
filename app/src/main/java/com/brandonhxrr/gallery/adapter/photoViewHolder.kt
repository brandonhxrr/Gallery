package com.brandonhxrr.gallery.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.PhotoView
import com.brandonhxrr.gallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager

class photoViewHolder(view: View ) : RecyclerView.ViewHolder(view){

    val id = view.findViewById<TextView>(R.id.item_number)
    val image = view.findViewById<ImageView>(R.id.item_photo)

    fun render(photoModel: Photo, glide: RequestBuilder<Bitmap>) {
        //id.text = photoModel.idPhoto
        //Glide.with(image.context).load(photoModel.path).centerCrop().placeholder(R.drawable.ic_image).into(image)

        glide.load(photoModel.path).centerCrop().into(image)

        image.setOnClickListener {
            val intent = Intent(it.context, PhotoView::class.java)
            intent.putExtra("path", photoModel.path)
            it.context.startActivity(intent)
        }
    }
}