package com.brandonhxrr.gallery.adapter.photo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.*
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.gson.Gson
import java.io.File

class PhotoViewHolder(
    view: View,
    val adapter: PhotoAdapter,
    private val showDeleteMenu: (Boolean, Number) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val image: ImageView = view.findViewById(R.id.item_photo)
    private val videoPlaceholder: ImageView = view.findViewById(R.id.placeholder)
    private val fileSelected: ImageView = view.findViewById(R.id.selected)

    @SuppressLint("NotifyDataSetChanged")
    fun render(
        photoModel: Photo,
        glide: RequestBuilder<Bitmap>,
        dataList: List<Photo>
    ) {

        val extension = File(photoModel.path).extension.lowercase()

        glide.load(photoModel.path).centerCrop().into(image)

        if (selectable) {
            fileSelected.visibility = View.VISIBLE
        } else {
            fileSelected.visibility = View.GONE
        }

        if (extension in imageExtensions) {
            videoPlaceholder.visibility = View.GONE
        } else {
            videoPlaceholder.visibility = View.VISIBLE
        }

        image.setOnLongClickListener {
            if (itemsList.isEmpty()) {
                dataList[photoModel.position].selected = true
                adapter.setSelectedItem(photoModel.position)
                Glide.with(image.context).load(R.drawable.file_selected).into(fileSelected)
                selectable = true
                bindingAdapter?.notifyDataSetChanged()
                showDeleteMenu(true, itemsList.size)
            }
            true
        }

        if (photoModel.selected) {
            Glide.with(image.context).load(R.drawable.file_selected).into(fileSelected)
        } else {
            Glide.with(image.context).load(R.drawable.file_unselected).into(fileSelected)
        }

        image.setOnClickListener {
            val limit: Int = if (dataList.size > 1000) 1000 else dataList.size
            val gson = Gson()
            val data = gson.toJson(dataList.subList(0, limit))

            val activityOptions: ActivityOptions =
                ActivityOptions.makeSceneTransitionAnimation(it.context as Activity)

            val intent = Intent(it.context, PhotoView::class.java)
            intent.putExtra("path", photoModel.path)
            intent.putExtra("data", data)
            intent.putExtra("position", photoModel.position)
            it.context.startActivity(intent, activityOptions.toBundle())
        }

        fileSelected.setOnClickListener {
            photoModel.selected = !photoModel.selected

            if (photoModel.selected) {
                dataList[photoModel.position].selected = true
                adapter.setSelectedItem(photoModel.position)
                Glide.with(image.context).load(R.drawable.file_selected).into(fileSelected)
            } else {
                dataList[photoModel.position].selected = false
                adapter.removeSelectedItem(photoModel.position)
                Glide.with(image.context).load(R.drawable.file_unselected).into(fileSelected)
            }

            if (itemsList.isEmpty()) {
                selectable = false
                showDeleteMenu(false, 0)
                bindingAdapter?.notifyDataSetChanged()
            } else {
                showDeleteMenu(true, itemsList.size)
            }
        }
    }
}