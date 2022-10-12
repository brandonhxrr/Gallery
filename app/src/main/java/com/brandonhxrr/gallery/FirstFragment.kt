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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.brandonhxrr.gallery.PhotoProvider.Companion.photoList
import com.brandonhxrr.gallery.adapter.photoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding

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
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = photoAdapter(fetchImages())
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
            photoList.add(Photo(i.toString(), imagecursor.getString(dataColumnIndex) ))
            Log.d("MSGF-P",  imagecursor.getString(dataColumnIndex));
        }
        return photoList
    }
}