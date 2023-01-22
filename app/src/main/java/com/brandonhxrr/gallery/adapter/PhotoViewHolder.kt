package com.brandonhxrr.gallery.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.PhotoView
import com.brandonhxrr.gallery.R
import com.bumptech.glide.RequestBuilder

class PhotoViewHolder(view: View ) : RecyclerView.ViewHolder(view){

    private val image : ImageView = view.findViewById(R.id.item_photo)

    fun render(photoModel: Photo, glide: RequestBuilder<Bitmap>) {

        glide.load(photoModel.path).centerCrop().into(image)

        image.setOnClickListener {
            val intent = Intent(it.context, PhotoView::class.java)
            intent.putExtra("path", photoModel.path)
            it.context.startActivity(intent)
        }
    }
}