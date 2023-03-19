package com.brandonhxrr.gallery.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.*
import com.bumptech.glide.RequestBuilder

class PhotoAdapter(
    private val photoList: List<Photo>,
    private val glide: RequestBuilder<Bitmap>,
    private val showDeleteMenu: (Boolean, Number) -> Unit
) : RecyclerView.Adapter<PhotoViewHolder>() {
    private var pageNumber: Int = 1
    private val limitPage = (photoList.size / 100) + 1
    var dataList : List<Photo> = getImagesFromPage(pageNumber, photoList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(layoutInflater.inflate(R.layout.photo, parent, false), this){ show, items ->
            showDeleteMenu(show, items)

        }
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = Photo(dataList[position].path, position, dataList[position].selected)
        holder.render(item, glide, photoList)
    }

    override fun getItemCount(): Int = dataList.size

    @SuppressLint("NotifyDataSetChanged")
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
        if(!itemsList.contains(dataList[position])){
            itemsList.add(dataList[position])
        }
    }

    fun resetItemsSelected() {
        for(item in dataList){
            item.selected = false
        }
    }

    fun removeSelectedItem(position: Int){
        dataList[position].selected = false
        itemsList.remove(dataList[position])
    }

    fun selectAllItems() {
        itemsList.clear()
        for(item in dataList){
            item.selected = true
            itemsList.add(item)
        }
    }
}