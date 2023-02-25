package com.brandonhxrr.gallery

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    private var deletedImageUri: Uri? = null
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var currentFile: File
    private lateinit var destinationPath: String
    private lateinit var deleteButton: ImageButton

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

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == AppCompatActivity.RESULT_OK) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deletePhotoFromExternal(requireContext(), deletedImageUri ?: return@launch, intentSenderLauncher)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "File couldn't be deleted", Toast.LENGTH_SHORT).show()
            }
        }
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
        deleteButton = selectableToolbar.findViewById(R.id.btn_delete)
        selectableToolbar.inflateMenu(R.menu.menu_selectable)
        selectableToolbar.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId){
                R.id.menu_copy -> {
                    val selectionIntent = Intent(requireContext(), AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "COPY"
                    true
                }
                R.id.menu_move -> {
                    val selectionIntent = Intent(requireContext(), AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "MOVE"
                    true
                }
                else -> false
            }
        }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar ${itemsList.size} archivos")
                .setPositiveButton("Eliminar") { _, _ ->

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
                            Toast.makeText(requireContext(), "File couldn't be deleted", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    Toast.makeText(requireContext(), "Files deleted", Toast.LENGTH_SHORT).show()
                    disableSelectable()
                    updateAdapterData()
                }
                .setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                }).show()
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
                    alertProgress.setTitle("Moviendo archivos")
                    val alertShow = alertProgress.show()
                    lifecycleScope.launch(Dispatchers.IO) {
                        for (item in itemsList) {
                            currentFile = File(item.path)
                            copyFileToUri(requireContext(), currentFile, destinationPath, true, requestPermissionLauncher, intentSenderLauncher)
                            currentOperation++

                            withContext(Dispatchers.Main) {
                                progressBar.progress = currentOperation
                                progressText.text = "$currentOperation/${itemsList.size}"
                            }
                        }
                        withContext(Dispatchers.Main) {
                            alertShow.dismiss()
                            Toast.makeText(context, "Files moved successfully", Toast.LENGTH_SHORT).show()
                            showDeleteMenu(false, 0)
                        }
                    }

                }
                "COPY" -> {
                    alertProgress.setTitle("Copiando archivos")
                    val alertShow = alertProgress.show()
                    lifecycleScope.launch(Dispatchers.IO) {
                        for (item in itemsList) {
                            currentFile = File(item.path)
                            copyFileToUri(requireContext(), currentFile, destinationPath, false, requestPermissionLauncher, intentSenderLauncher)
                            currentOperation++

                            withContext(Dispatchers.Main) {
                                progressBar.progress = currentOperation
                                progressText.text = "$currentOperation/${itemsList.size}"
                            }
                        }
                        withContext(Dispatchers.Main) {
                            alertShow.dismiss()
                            Toast.makeText(context, "Files copied successfully", Toast.LENGTH_SHORT).show()
                            showDeleteMenu(false, 0)
                        }
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

    private fun disableSelectable(){
        selectableToolbar.visibility = View.GONE
        toolbar.visibility = View.VISIBLE
        itemsList.clear()
        selectable = false
        (recyclerView.adapter as PhotoAdapter).resetItemsSelected()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun updateAdapterData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val media = getAllImagesAndVideosSortedByRecent(requireContext())
            val glide = Glide.with(requireContext())
            val builder = glide.asBitmap()

            withContext(Dispatchers.Main) {
                myAdapter = PhotoAdapter(media, builder) { show, items ->
                    showDeleteMenu(show, items)
                }
                recyclerView.swapAdapter(myAdapter, false)
            }
        }
    }

}