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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.photoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding
import com.bumptech.glide.Glide
import java.io.File
import java.util.Collections

class ViewAlbum : Fragment() {
    private var pathAlbum: String? = null
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pathAlbum = it.getString("pathAlbum")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())
        return binding.root
        //return inflater.inflate(R.layout.fragment_view_album, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(context: Context) {
        val recyclerView = binding.gridRecyclerView

        val glide = Glide.with(this)
        val builder = glide.asBitmap()

        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = photoAdapter(fetchImages(), builder)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> glide.resumeRequests()
                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL, AbsListView.OnScrollListener.SCROLL_STATE_FLING -> glide.pauseRequests()
                }
            }
        })
    }

    fun fetchImages(): List<Photo> {
        val photoList: ArrayList<Photo> = ArrayList()

        val fileList: List<File> = getImagesFromFolder(requireContext(), pathAlbum.toString())

        for (file in fileList) {
            photoList.add(Photo(file.path))
        }

        photoList.reverse()

        return photoList
    }

    companion object {
        @JvmStatic
        fun newInstance(pathAlbum: String) =
            ViewAlbum().apply {
                arguments = Bundle().apply {
                    putString("pathAlbum", pathAlbum)
                }
            }
    }
}