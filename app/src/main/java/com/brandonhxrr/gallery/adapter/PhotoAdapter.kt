package com.brandonhxrr.gallery.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R
import com.brandonhxrr.gallery.getImagesFromPage
import com.brandonhxrr.gallery.selectable
import com.bumptech.glide.RequestBuilder

class PhotoAdapter(
    private val photoList: List<Photo>,
    private val glide: RequestBuilder<Bitmap>
) : RecyclerView.Adapter<PhotoViewHolder>() {
    private var pageNumber: Int = 1
    private val limitPage = (photoList.size / 100) + 1
    var dataList : List<Photo> = getImagesFromPage(pageNumber, photoList)

    var isInSelectionMode: Boolean = false
    var itemsList = mutableListOf<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(layoutInflater.inflate(R.layout.photo, parent, false), this)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = Photo(dataList[position].path, position, dataList[position].selected)
        holder.render(item, glide, photoList)
    }

    override fun getItemCount(): Int = dataList.size

    fun addMoreData() : Boolean{
        if(pageNumber < limitPage) {
            pageNumber++
            dataList = dataList.plus(getImagesFromPage(pageNumber, photoList))
            this.notifyDataSetChanged()
            return true
        }
        return false
    }

    fun setSelectedItem(position: Int){
        dataList[position].selected = true
        itemsList.add(dataList[position])
    }

    fun removeSelectedItem(position: Int){
        dataList[position].selected = false
        itemsList.remove(dataList[position])
    }
}