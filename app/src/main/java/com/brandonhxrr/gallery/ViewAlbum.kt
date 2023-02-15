package com.brandonhxrr.gallery

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.PhotoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewAlbum : Fragment() {
    private lateinit var album : Album
    private lateinit var toolbar: Toolbar
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var loading = true

    private lateinit var builder: RequestBuilder<Bitmap>
    private lateinit var myAdapter: PhotoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var media: List<Photo>
    private lateinit var txtAlbumEmpty: MaterialTextView
    private lateinit var selectableToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val gson = Gson()
            album = gson.fromJson(it.getString("albumData"), Album::class.java)
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
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.app_logo).visibility = View.GONE

        toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        selectableToolbar = (activity as AppCompatActivity).findViewById(R.id.selectable_toolbar)
        txtAlbumEmpty = (activity as AppCompatActivity).findViewById(R.id.album_empty)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        (activity as AppCompatActivity).supportActionBar?.title = ""
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.textAppbar).text = getString(R.string.app_name)
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.app_logo).visibility = View.VISIBLE
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(false)
        txtAlbumEmpty.visibility = View.GONE

        if(selectableToolbar.visibility == View.VISIBLE){
            showDeleteMenu(false, 0)
            itemsList.clear()
            selectable = false
            myAdapter.resetItemsSelected()
            myAdapter.notifyDataSetChanged()
        }

        selectableToolbar.menu.clear()

        _binding = null
    }

    private fun initRecyclerView(context: Context) {
        recyclerView = binding.gridRecyclerView

        val glide = Glide.with(this)
        builder = glide.asBitmap()

        media = getImagesFromAlbum(album.path)

        myAdapter = PhotoAdapter(media, builder) { show, items ->
            showDeleteMenu(show, items)
        }

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = myAdapter
        setUpPagination()
    }

    private fun setUpPagination() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

               if(dy > 0) {
                    visibleItemCount = recyclerView.childCount
                    totalItemCount = recyclerView.layoutManager!!.itemCount
                    pastVisibleItems = (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

                    if(loading) {
                        if(visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loading = false
                            myAdapter.addMoreData()
                            loading = true
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

    override fun onResume() {
        super.onResume()

        selectableToolbar.inflateMenu(R.menu.menu_selectable_album)

        selectableToolbar.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId){
                R.id.menu_copy -> {
                    Toast.makeText(requireContext(), "COPY", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_move -> {
                    Toast.makeText(requireContext(), "MOVE", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_select_all -> {
                    myAdapter.selectAllItems()
                    showDeleteMenu(true, itemsList.size)
                    myAdapter.notifyDataSetChanged()
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            media = getImagesFromAlbum(album.path)
            album.itemsNumber = getImageVideoNumber(File(album.path))

            withContext(Dispatchers.Main){
                myAdapter = PhotoAdapter(media, builder) { show, items ->
                    showDeleteMenu(show, items)
                }
                recyclerView.swapAdapter(myAdapter, false)

                if(media.isNotEmpty()){
                    (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.textAppbar).text =
                        "${album.name} (${album.itemsNumber})"
                }else {
                    albumes?.remove(File(album.path))
                    (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.textAppbar).text =
                        "${album.name} (0)"
                    txtAlbumEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showDeleteMenu(show: Boolean, items: Number) {
        when(show){
            true -> {
                toolbar.visibility = View.GONE
                selectableToolbar.visibility = View.VISIBLE
                (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.text_items_num).text = items.toString()
                (activity as AppCompatActivity).findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
                    showDeleteMenu(false, 0)
                    itemsList.clear()
                    selectable = false
                    myAdapter.resetItemsSelected()
                    myAdapter.notifyDataSetChanged()
                }
            }
            false -> {
                toolbar.visibility = View.VISIBLE
                selectableToolbar.visibility = View.GONE
            }
        }
    }
}