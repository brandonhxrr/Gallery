package com.brandonhxrr.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.PhotoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson

class ViewAlbum : Fragment() {
    private lateinit var album : Album
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var loading = true

    private var myAdapter: PhotoAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var media: List<Photo>? = null
    private var dataList: List<Photo>? = null

    private var pageNumber = 1
    private var limitPage : Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val gson = Gson()
            album = gson.fromJson(it.get("albumData") as String , Album::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())

        (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.textAppbar).text =
            "${album.name} (${album.itemsNumber})"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.textAppbar).text = getString(R.string.app_name)
        _binding = null
    }

    private fun initRecyclerView(context: Context) {
        recyclerView = binding.gridRecyclerView

        val glide = Glide.with(this)
        val builder = glide.asBitmap()

        media = fetchImages()

        limitPage = (media!!.size / 100) + 1

        Log.d("Visible106", limitPage.toString())

        dataList = getImagesFromPage(pageNumber, media!!)

        myAdapter = PhotoAdapter(dataList!!, builder, R.layout.photo3)

        recyclerView!!.itemAnimator = DefaultItemAnimator()

        recyclerView!!.isNestedScrollingEnabled = false

        recyclerView!!.layoutManager = GridLayoutManager(context, 3)
        recyclerView!!.adapter = myAdapter

        setUpPagination(glide)
    }

    private fun fetchImages(): List<Photo> {
        return getImagesFromAlbum(album.path)
    }

    private fun getImagesFromPage(page: Int, data: List<Photo>): List<Photo> {
        val startIndex = (page - 1) * 100
        val endIndex = startIndex + 100

        if (startIndex >= data.size) {
            return emptyList()
        }

        val end = if (endIndex > data.size) data.size else endIndex

        return data.subList(startIndex, end)
    }

    private fun setUpPagination(glide: RequestManager) {
        Log.d("Visible103", recyclerView.toString())
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

               if(dy > 0) {
                visibleItemCount = recyclerView.childCount
                totalItemCount = recyclerView.layoutManager!!.itemCount
                pastVisibleItems = (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

                    if(loading) {
                        if(visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loading = false
                            if(pageNumber < limitPage) {
                                pageNumber++
                                dataList = dataList?.plus(getImagesFromPage(pageNumber, media!!))
                                myAdapter!!.dataList = dataList as List<Photo>
                                myAdapter!!.notifyDataSetChanged()
                                loading = true
                            }
                        }
                   }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                onScrolled(recyclerView, recyclerView.scrollX, recyclerView.scrollY)
            }
        })
    }
}