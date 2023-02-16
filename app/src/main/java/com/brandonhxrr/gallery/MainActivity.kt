package com.brandonhxrr.gallery

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.RecyclerView
import com.brandonhxrr.gallery.adapter.PhotoAdapter
import com.brandonhxrr.gallery.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var selectableToolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var myAdapter: PhotoAdapter
    private var deletedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        bottomNavView = findViewById(R.id.bottomNavigationView)
        toolbar = findViewById(R.id.toolbar)
        selectableToolbar = findViewById(R.id.selectable_toolbar)
        deleteButton = findViewById(R.id.btn_delete)
        shareButton = findViewById(R.id.btn_share)

        bottomNavView.setItemOnTouchListener(R.id.menu_photos) { v, _ ->
            if (navController.currentDestination?.id == R.id.SecondFragment) {
                navController.popBackStack()
            } else if (navController.currentDestination?.id == R.id.ViewAlbumFragment) {
                navController.popBackStack(R.id.FirstFragment, false)
            }
            v.performClick()
            true
        }

        bottomNavView.setItemOnTouchListener(R.id.menu_album) { v, _ ->
            if (navController.currentDestination?.id == R.id.FirstFragment) {
                navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
            } else if (navController.currentDestination?.id == R.id.ViewAlbumFragment) {
                navController.popBackStack()
            }
            v.performClick()
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(selectableToolbar.visibility == View.VISIBLE){
                    disableSelectable()
                }else {
                    when(navController.currentDestination?.id) {
                        R.id.SecondFragment -> {
                            bottomNavView.selectedItemId = R.id.menu_photos
                        }
                    }
                    navController.navigateUp()
                }
            }
        })

        shareButton.setOnClickListener {
            val intentShare = Intent(Intent.ACTION_SEND_MULTIPLE)
            intentShare.type = "image/*"

            val uriList = arrayListOf<Uri>()
            for(item in itemsList){
                uriList.add(FileProvider.getUriForFile(this, "${this.packageName}.provider", File(item.path)))
            }
            intentShare.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
            startActivity(intentShare)
        }

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == RESULT_OK) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deletePhotoFromExternal(this@MainActivity, deletedImageUri ?: return@launch, intentSenderLauncher)
                    }
                }
            } else {
                Toast.makeText(this, "File couldn't be deleted", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar ${itemsList.size} archivos")
                .setPositiveButton("Eliminar") { _, _ ->
                    recyclerView = findViewById(R.id.gridRecyclerView)

                    for (item in itemsList) {
                        val currentFile = File(item.path)

                        if (currentFile.delete()) {
                            recyclerView.adapter?.notifyItemRemoved(item.position)
                        } else if (deletePhotoFromExternal(
                                this,
                                getContentUri(this, currentFile)!!,
                                intentSenderLauncher
                            )
                        ) {
                            recyclerView.adapter?.notifyItemRemoved(item.position)
                        } else {
                            Toast.makeText(this, "File couldn't be deleted", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    Toast.makeText(this, "Files deleted", Toast.LENGTH_SHORT).show()
                    disableSelectable()
                    updateAdapterData()
                }
                .setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                }).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun disableSelectable(){
        recyclerView = findViewById(R.id.gridRecyclerView)
        selectableToolbar.visibility = View.GONE
        toolbar.visibility = View.VISIBLE
        itemsList.clear()
        selectable = false
        (recyclerView.adapter as PhotoAdapter).resetItemsSelected()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun updateAdapterData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val media = getAllImagesAndVideosSortedByRecent(this@MainActivity)
            val glide = Glide.with(this@MainActivity)
            val builder = glide.asBitmap()

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
                findViewById<MaterialTextView>(R.id.text_items_num).text = items.toString()
                findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
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