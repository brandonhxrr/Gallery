package com.brandonhxrr.gallery

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.album.AlbumAdapter
import com.brandonhxrr.gallery.databinding.FragmentAlbumsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var albums: HashMap<File, List<File>>
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var builder: RequestBuilder<Bitmap>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albums = albumes!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(context: Context) {
        recyclerView = binding.gridRecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val glide = Glide.with(this)
        builder = glide.asBitmap()

        albumAdapter = AlbumAdapter(builder)
        albumAdapter.setItems(albums)

        recyclerView.adapter = albumAdapter
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {

            albums = sortImagesByFolder(getAllImages(requireContext())) as HashMap<File, List<File>>

            withContext(Dispatchers.Main) {
                if(albums.isNotEmpty()){
                    albumAdapter = AlbumAdapter(builder)
                    albumAdapter.setItems(albums)
                    recyclerView.swapAdapter(albumAdapter, false)

                }else {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }
        }
    }
}
