package com.brandonhxrr.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var currentFile: File
    private lateinit var destinationPath: String
    private var deletedImageUri: Uri? = null
    private lateinit var operation: String
    private lateinit var deleteButton: ImageButton
    private lateinit var txtItemsNum: TextView
    private lateinit var textAppbar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val gson = Gson()
            album = gson.fromJson(it.getString("albumData"), Album::class.java)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initRecyclerView(requireContext())

        (activity as AppCompatActivity).findViewById<ImageView>(R.id.app_logo).visibility = View.GONE

        toolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        selectableToolbar = (activity as AppCompatActivity).findViewById(R.id.selectable_toolbar)
        txtAlbumEmpty = (activity as AppCompatActivity).findViewById(R.id.album_empty)
        deleteButton = selectableToolbar.findViewById(R.id.btn_delete)
        txtItemsNum = selectableToolbar.findViewById(R.id.text_items_num)
        textAppbar = (activity as AppCompatActivity).findViewById<MaterialTextView>(R.id.textAppbar)
        textAppbar.text = getString(R.string.album_detail, album.name, album.itemsNumber)


        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        (activity as AppCompatActivity).supportActionBar?.title = ""

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == AppCompatActivity.RESULT_OK) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deletePhotoFromExternal(requireContext(), deletedImageUri ?: return@launch, intentSenderLauncher)
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.file_not_deleted), Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_n_files, itemsList.size.toString()))
                .setPositiveButton(getString(R.string.menu_delete)) { _, _ ->

                    for (item in itemsList) {
                        val currentFile = File(item.path)

                        if (currentFile.delete()) {
                            recyclerView.adapter?.notifyItemRemoved(item.position)
                        } else if (deletePhotoFromExternal(
                                requireContext(),
                                getContentUri(requireContext(), currentFile)!!,
                                intentSenderLauncher
                            )
                        ) {
                            recyclerView.adapter?.notifyItemRemoved(item.position)
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.file_not_deleted), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    Toast.makeText(requireContext(), getString(R.string.files_deleted), Toast.LENGTH_SHORT).show()
                    disableSelectable()
                    updateAdapterData()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        txtItemsNum.setOnClickListener {
            myAdapter.selectAllItems()
            myAdapter.notifyDataSetChanged()
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDestroyView() {
        super.onDestroyView()
        textAppbar.text = getString(R.string.app_name)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        selectableToolbar.inflateMenu(R.menu.menu_selectable_album)

        selectableToolbar.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId){
                R.id.menu_copy -> {
                    val selectionIntent = Intent(requireContext(), AlbumSelection::class.java)
                    operation = "COPY"
                    selectionIntent.putExtra("operation", operation)
                    resultLauncher.launch(selectionIntent)
                    true
                }
                R.id.menu_move -> {
                    val selectionIntent = Intent(requireContext(), AlbumSelection::class.java)
                    operation = "MOVE"
                    selectionIntent.putExtra("operation", operation)
                    resultLauncher.launch(selectionIntent)
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

                if(media.isEmpty()){
                    albumes?.remove(File(album.path))
                    txtAlbumEmpty.visibility = View.VISIBLE
                }

                textAppbar.text = getString(R.string.album_detail, album.name, media.size.toString())
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteMenu(show: Boolean, items: Number = 0) {
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
                itemsList.clear()
                selectable = false
                myAdapter.resetItemsSelected()
                myAdapter.notifyDataSetChanged()
            }
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            destinationPath = data?.getStringExtra("RUTA")!!

            val view = layoutInflater.inflate(R.layout.alert_progress, null)
            val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)
            val progressText = view.findViewById<TextView>(R.id.text_progress)
            progressBar.max = itemsList.size
            progressBar.progress = 0

            val alertProgress = MaterialAlertDialogBuilder(requireContext())
            alertProgress.setCancelable(false)
            alertProgress.setView(view)

            var currentOperation = 0

            when(operation) {
                "MOVE" -> {
                    alertProgress.setTitle(getString(R.string.moving_files))
                    val alertShow = alertProgress.show()
                    lifecycleScope.launch(Dispatchers.Main) {
                        for (item in itemsList) {
                                try{
                                    currentFile = File(item.path)
                                    progressBar.progress = currentOperation
                                    progressText.text = getString(R.string.operation_detail, currentOperation.toString(), itemsList.size.toString())
                                    currentOperation++

                                    withContext(Dispatchers.IO) {
                                        copyFileToUri(requireContext(), currentFile, destinationPath, true, requestPermissionLauncher, intentSenderLauncher)
                                    }
                                }catch(e: Exception){
                                    Log.e("COPY_ERROR", "Error moving file: ${e.message}")
                                }
                        }
                        alertShow.dismiss()
                        Toast.makeText(context, getString(R.string.files_moved), Toast.LENGTH_SHORT).show()
                        showDeleteMenu(false)
                        updateAdapterData()
                    }
                }
                "COPY" -> {
                    alertProgress.setTitle(getString(R.string.copying_files))
                    val alertShow = alertProgress.show()
                    lifecycleScope.launch(Dispatchers.Main) {
                        for (item in itemsList) {
                            try{
                                currentFile = File(item.path)
                                progressBar.progress = currentOperation
                                progressText.text = getString(R.string.operation_detail, currentOperation.toString(), itemsList.size.toString())
                                currentOperation++

                                withContext(Dispatchers.IO) {
                                    copyFileToUri(requireContext(), currentFile, destinationPath, false, requestPermissionLauncher, intentSenderLauncher)
                                }
                            }catch(e: Exception){
                                Log.e("COPY_ERROR", "Error copying file: ${e.message}")
                            }

                        }
                        alertShow.dismiss()
                        Toast.makeText(context, getString(R.string.files_copied), Toast.LENGTH_SHORT).show()
                        showDeleteMenu(false)
                        updateAdapterData()
                    }
                }
            }

        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)

                val sharedPreferences = requireContext().getSharedPreferences(PERMISSION_PREFS_NAME, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean(SD_CARD_PERMISSION_GRANTED_KEY, true)
                editor.apply()
                copyToExternal(requireContext(), currentFile, destinationPath, operation == "MOVE", intentSenderLauncher)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun disableSelectable(){
        recyclerView = (context as Activity).findViewById(R.id.gridRecyclerView)
        selectableToolbar.visibility = View.GONE
        toolbar.visibility = View.VISIBLE
        itemsList.clear()
        selectable = false
        (recyclerView.adapter as PhotoAdapter).resetItemsSelected()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun updateAdapterData(){

        val media = getImagesFromAlbum(album.path)

        if(media.isEmpty()){
            albumes?.remove(File(album.path))
            txtAlbumEmpty.visibility = View.VISIBLE
        }

        textAppbar.text = getString(R.string.album_detail, album.name, media.size.toString())

        val glide = Glide.with(requireContext())
        val builder = glide.asBitmap()

        myAdapter = PhotoAdapter(media, builder) { show, items ->
            showDeleteMenu(show, items)
        }
        recyclerView.swapAdapter(myAdapter, false)
    }

}