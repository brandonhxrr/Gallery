package com.brandonhxrr.gallery

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.PhotoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

class FirstFragment : Fragment() {

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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(context:Context) {
        recyclerView = binding.gridRecyclerView

        val glide = Glide.with(this)
        val builder = glide.asBitmap()

        media = getAllImagesAndVideosSortedByRecent(context)
        limitPage = (media!!.size / 100) + 1

        dataList = getImagesFromPage(pageNumber, media!!)

        myAdapter = PhotoAdapter(dataList!!, builder, R.layout.photo)

        recyclerView!!.itemAnimator = DefaultItemAnimator()

        recyclerView!!.isNestedScrollingEnabled = false

        recyclerView!!.layoutManager = GridLayoutManager(context, 4)
        recyclerView!!.adapter = myAdapter

        setUpPagination()
    }

    private fun setUpPagination() {
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