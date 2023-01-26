package com.brandonhxrr.gallery.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.PhotoView
import com.brandonhxrr.gallery.R
import com.bumptech.glide.RequestBuilder
import java.io.File

class PhotoViewHolder(view: View ) : RecyclerView.ViewHolder(view){

    private val image : ImageView = view.findViewById(R.id.item_photo)
    private val videoPlaceholder : ImageView = view.findViewById(R.id.placeholder)

    fun render(photoModel: Photo, glide: RequestBuilder<Bitmap>) {

        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp")
        val videoExtensions = arrayOf("mp4", "mkv", "avi", "wmv", "mov")

        val extension = File(photoModel.path).extension

        glide.load(photoModel.path).centerCrop().into(image)

        if(extension in imageExtensions){
            videoPlaceholder.visibility = View.INVISIBLE

            image.setOnClickListener {
                val intent = Intent(it.context, PhotoView::class.java)
                intent.putExtra("path", photoModel.path)
                it.context.startActivity(intent)
            }
        }else if(extension in videoExtensions) {
            image.setOnClickListener {
                val videoUri: Uri = Uri.parse(photoModel.path)
                val intent = Intent(Intent.ACTION_VIEW,videoUri)
                intent.setDataAndType(videoUri, "video/*")
                it.context.startActivity(intent)
            }
        }

    }
}