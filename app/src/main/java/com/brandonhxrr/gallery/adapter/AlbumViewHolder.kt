package com.brandonhxrr.gallery.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.Album
import com.brandonhxrr.gallery.R
import com.brandonhxrr.gallery.getImageVideoNumber
import com.bumptech.glide.RequestBuilder
import com.google.gson.Gson
import java.io.File

class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val image: ImageView = view.findViewById(R.id.album_image)
    private val title: TextView = view.findViewById(R.id.album_title)
    private val counter: TextView = view.findViewById(R.id.album_counter)

    companion object {
        fun new(viewGroup: ViewGroup) = AlbumViewHolder(
            LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.album, viewGroup, false))
    }

    fun bind(files: List<File>?, glide: RequestBuilder<Bitmap>) {
    //fun bind(folder: List<File>?, glide: RequestBuilder<Bitmap>) {

        //Log.d("PARENT100: ", folder.path)

        //val files = getImagesFromFolder(image.context, folder.path)

        //Log.d("PARENT101: ", files.size.toString())
        if (files != null) {
            (files.isNotEmpty()).let {
                val firstChild = files[0]
                val parent = File(firstChild.parent as String)

                //Log.d("PARENT99: ", parent.path)
                //Log.d("PARENT98: ", parent.list()[0])
                //Log.d("PARENT97: ", parent.listFiles()[0].absolutePath)


                title.text = parent.nameWithoutExtension
                glide.load(firstChild).centerCrop().into(image)

                var items = 0
                try{
                    items = getImageVideoNumber(parent)
                }catch (e : Exception){
                    Log.d("PARENT96", "HUBO UN ERROR" + e.message)
                }



                //val items = parent.listFiles()?.size!!
                counter.text = if (items > 1) items.toString() + " items" else parent.listFiles()?.size.toString() + " item"

                image.setOnClickListener {
                    val gson = Gson()
                    val album : Album = Album(parent.absolutePath, parent.nameWithoutExtension, items)
                    val albumData = gson.toJson(album)
                    val bundle = bundleOf("albumData" to albumData)
                    it.findNavController().navigate(R.id.action_SecondFragment_to_ViewAlbumFragment, bundle)
                }
            }
        }


    }

    /*fun bind(files: List<String>?, glide: RequestBuilder<Bitmap>) {
        (files?.isNotEmpty()).let {
            val firstChild = files!![0]
            val parent = File(File(firstChild).parent as String)

            title.text = parent.nameWithoutExtension
            glide.load(firstChild).centerCrop().into(image)

            val items = parent.listFiles()?.size!!

            counter.text = if (items > 1)  parent.listFiles()?.size.toString() + " items" else parent.listFiles()?.size.toString() + " item"

            image.setOnClickListener {
                val bundle = bundleOf("pathAlbum" to parent.absolutePath)
                it.findNavController().navigate(R.id.action_SecondFragment_to_ViewAlbumFragment, bundle)
            }
        }
    }*/
}