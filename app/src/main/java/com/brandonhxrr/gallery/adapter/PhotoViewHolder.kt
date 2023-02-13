package com.brandonhxrr.gallery.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.*
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.gson.Gson
import java.io.File

class PhotoViewHolder(view: View,val adapter: PhotoAdapter) : RecyclerView.ViewHolder(view){

    private val image : ImageView = view.findViewById(R.id.item_photo)
    private val videoPlaceholder : ImageView = view.findViewById(R.id.placeholder)
    private val fileSelected: ImageView = view.findViewById(R.id.selected)

    fun render(
        photoModel: Photo,
        glide: RequestBuilder<Bitmap>,
        dataList: List<Photo>
    ) {

        val extension = File(photoModel.path).extension

        glide.load(photoModel.path).centerCrop().into(image)

        if(selectable){
            fileSelected.visibility = View.VISIBLE
        }else {
            fileSelected.visibility = View.GONE
        }

        if(extension in imageExtensions){
            videoPlaceholder.visibility = View.GONE
        }else {
            videoPlaceholder.visibility = View.VISIBLE
        }

        image.setOnLongClickListener {
            if(adapter.itemsList.isEmpty()){
                dataList[photoModel.position].selected = true
                adapter.setSelectedItem(photoModel.position)
                Glide.with(image.context).load(R.drawable.file_selected).into(fileSelected)
                Log.d("COPY100: SItems",adapter.itemsList.toString())
                selectable = true
                bindingAdapter?.notifyDataSetChanged()
            }
            true
        }

        if(dataList[photoModel.position].selected){
            Glide.with(image.context).load(R.drawable.file_selected).into(fileSelected)
        }else {
            Glide.with(image.context).load(R.drawable.file_unselected).into(fileSelected)
        }

        image.setOnClickListener {
            if(extension in imageExtensions){
                val limit : Int = if(dataList.size > 2000) 2000 else dataList.size
                val gson = Gson()
                val data = gson.toJson(dataList.subList(0, limit))

                val activityOptions: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(it.context as Activity)

                val intent = Intent(it.context, PhotoView::class.java)
                intent.putExtra("path", photoModel.path)
                intent.putExtra("data", data)
                intent.putExtra("position", photoModel.position)
                it.context.startActivity(intent, activityOptions.toBundle())
            } else {
                val videoUri: Uri = Uri.parse(photoModel.path)
                val intent = Intent(Intent.ACTION_VIEW,videoUri)
                intent.setDataAndType(videoUri, "video/*")
                it.context.startActivity(intent)
            }
        }

        fileSelected.setOnClickListener {
            photoModel.selected = !photoModel.selected
            if(photoModel.selected){
                dataList[photoModel.position].selected = true
                adapter.setSelectedItem(photoModel.position)
                Glide.with(image.context).load(R.drawable.file_selected).into(fileSelected)
            } else{
                dataList[photoModel.position].selected = false
                adapter.removeSelectedItem(photoModel.position)
                Glide.with(image.context).load(R.drawable.file_unselected).into(fileSelected)
            }
        }
    }
}