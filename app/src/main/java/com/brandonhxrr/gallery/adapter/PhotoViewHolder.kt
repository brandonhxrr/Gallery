package com.brandonhxrr.gallery.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.Visibility
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.PhotoView
import com.brandonhxrr.gallery.R
import com.bumptech.glide.RequestBuilder
import com.google.gson.Gson
import java.io.File

class PhotoViewHolder(view: View ) : RecyclerView.ViewHolder(view){

    private val image : ImageView = view.findViewById(R.id.item_photo)
    private val videoPlaceholder : ImageView = view.findViewById(R.id.placeholder)

    fun render(photoModel: Photo, glide: RequestBuilder<Bitmap>, dataList: List<Photo>) {

        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp")
        val videoExtensions = arrayOf("mp4", "mkv", "avi", "wmv", "mov")

        val extension = File(photoModel.path).extension

        glide.load(photoModel.path).centerCrop().into(image)

        if(extension in imageExtensions){

            image.setOnClickListener {

                val limit : Int = if(dataList.size > 2000) 2000 else dataList.size
                val gson = Gson()
                val data = gson.toJson(dataList.subList(0, limit))

                val intent = Intent(it.context, PhotoView::class.java)
                intent.putExtra("path", photoModel.path)
                intent.putExtra("data", data)
                intent.putExtra("position", photoModel.position)
                it.context.startActivity(intent)
            }
            videoPlaceholder.visibility = View.INVISIBLE
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