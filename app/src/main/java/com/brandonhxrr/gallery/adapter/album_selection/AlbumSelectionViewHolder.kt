package com.brandonhxrr.gallery.adapter.album_selection

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.R
import com.brandonhxrr.gallery.getImageVideoNumber
import com.bumptech.glide.RequestBuilder
import java.io.File

class AlbumSelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val image: ImageView = view.findViewById(R.id.album_image)
    private val title: TextView = view.findViewById(R.id.album_title)
    private val counter: TextView = view.findViewById(R.id.album_counter)

    companion object {
        fun new(viewGroup: ViewGroup) = AlbumSelectionViewHolder(
            LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.album, viewGroup, false))
    }

    fun bind(files: List<File>?, glide: RequestBuilder<Bitmap>) {
        if (files != null) {
            (files.isNotEmpty()).let {
                val firstChild = files[0]
                val parent = File(firstChild.parent as String)

                title.text = parent.nameWithoutExtension
                glide.load(firstChild).centerCrop().into(image)

                var items = 0
                try{
                    items = getImageVideoNumber(parent)
                }catch (_: Exception){ }

                counter.text = if (items > 1) counter.context.getString(R.string.multi_items, items.toString()) else  counter.context.getString(R.string.single_item)

                image.setOnClickListener {
                    val intent = Intent()
                    intent.putExtra("RUTA", parent.absolutePath)
                    (it.context as Activity).setResult(Activity.RESULT_OK, intent)
                    (it.context as Activity).finish()
                }
            }
        }
    }
}