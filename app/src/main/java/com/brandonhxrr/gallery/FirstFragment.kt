package com.brandonhxrr.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.PhotoAdapter
import com.brandonhxrr.gallery.databinding.FragmentFirstBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var loading = true
    private lateinit var operation: String
    private lateinit var builder: RequestBuilder<Bitmap>
    private lateinit var myAdapter: PhotoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var media: List<Photo>
    private lateinit var toolbar: Toolbar
    private lateinit var selectableToolbar: Toolbar

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

    private fun initRecyclerView(context:Context) {
        recyclerView = binding.gridRecyclerView


        val glide = Glide.with(this)
        builder = glide.asBitmap()

        media = getAllImagesAndVideosSortedByRecent(context)

        myAdapter = PhotoAdapter(media, builder) { show, items ->
            showDeleteMenu(show, items)
        }
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = GridLayoutManager(context, 4)
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
        toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        selectableToolbar = (activity as AppCompatActivity).findViewById(R.id.selectable_toolbar)
        selectableToolbar.inflateMenu(R.menu.menu_selectable)
        selectableToolbar.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId){
                R.id.menu_copy -> {
                    Toast.makeText(requireContext(), "COPY", Toast.LENGTH_SHORT).show()
                    val selectionIntent = Intent(requireContext(), AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "COPY"
                    true
                }
                R.id.menu_move -> {
                    Toast.makeText(requireContext(), "MOVE", Toast.LENGTH_SHORT).show()
                    val selectionIntent = Intent(requireContext(), AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "MOVE"
                    true
                }
                else -> false
            }
        }

        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            media = getAllImagesAndVideosSortedByRecent(requireContext())

            withContext(Dispatchers.Main) {
                myAdapter = PhotoAdapter(media, builder) { show, items ->
                    showDeleteMenu(show, items)
                }
                recyclerView.swapAdapter(myAdapter, false)
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

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val ruta: String = data?.getStringExtra("RUTA")!!

            val dest = File(ruta)
            val uri = Uri.fromFile(dest)
            Log.d("COPY100: URI", uri.toString())

            when(operation) {
                "MOVE" -> {
                    //copyFilesToSD(ruta, currentFile, true)
                }
                "COPY" -> {
                    //copyFilesToSD(ruta, currentFile, false)
                }
            }
        }
    }

}