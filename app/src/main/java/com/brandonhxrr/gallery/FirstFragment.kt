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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.PhotoProvider.Companion.photoList
import com.brandonhxrr.gallery.adapter.photoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding
import com.bumptech.glide.Glide

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): FirstFragment = FirstFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(context:Context) {
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

    fun fetchImages(): ArrayList<Photo> {
        val photoList: ArrayList<Photo> = ArrayList()

        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID)
        val imagecursor: Cursor = requireActivity().managedQuery(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, ""
        )
        for (i in 0 until 120) { //imagecursor.count
            imagecursor.moveToPosition(i)
            val dataColumnIndex =
                imagecursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            photoList.add(Photo(imagecursor.getString(dataColumnIndex) ))
            Log.d("MSGF-P",  imagecursor.getString(dataColumnIndex));
        }
        return photoList
    }
}