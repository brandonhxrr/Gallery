package com.brandonhxrr.gallery

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.AlbumAdapter
import com.brandonhxrr.gallery.adapter.photoAdapter
import com.brandonhxrr.gallery.databinding.FragmentSecondBinding
import com.bumptech.glide.Glide

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(context: Context) {
        val recyclerView = binding.gridRecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val glide = Glide.with(this)
        val builder = glide.asBitmap()

        val fol: AlbumAdapter = AlbumAdapter(builder)
        fol.setItems(sortImagesByFolder(getAllImages(context)))

        recyclerView.adapter = fol

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
}