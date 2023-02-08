package com.brandonhxrr.gallery

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.brandonhxrr.gallery.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val albums = intent.extras?.get("albums")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        //appBarConfiguration = AppBarConfiguration(navController.graph)

        bottomNavView = findViewById(R.id.bottomNavigationView)

        logs()

        bottomNavView.setOnItemSelectedListener {
            menuItem ->
            when(menuItem.itemId) {
                R.id.menu_photos -> {
                    if (navController.currentDestination?.id == R.id.SecondFragment){
                        navController.popBackStack()
                    } else if(navController.currentDestination?.id == R.id.ViewAlbumFragment) {
                        navController.popBackStack(R.id.FirstFragment, false)
                    }
                    true
                }

                R.id.menu_album -> {
                    if (navController.currentDestination?.id == R.id.FirstFragment){
                        val bundle = bundleOf("albums" to albums)
                        navController.navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
                        //navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }else if(navController.currentDestination?.id == R.id.ViewAlbumFragment) {
                        navController.popBackStack()
                    }
                    true
                }

                else -> false
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        val navUp = navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        return navUp
    }

    private fun logs(){
        Log.d("COPY100: INTERNAL", MediaStore.Images.Media.INTERNAL_CONTENT_URI.path.toString())
        Log.d("COPY100: EXTERNAL", MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path.toString())
        Log.d("COPY100: FILESDIRE", getExternalFilesDir("").toString())
        Log.d("COPY100: FILESDIRN", getExternalFilesDir(null).toString())
        Log.d("COPY100: ENV", Environment.getExternalStorageDirectory().path)
        Log.d("COPY100: EXTERNAL", MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path.toString())

        Log.d("COPY100: EXTERNAL",
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY).path!!
        )
        Log.d("COPY100: EXTERNAL",
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL).path!!
        )
        Log.d("COPY100: EXTERNAL",
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL).path!!
        )

        val dirs = getExternalFilesDirs(null)

        for(dir in dirs) {
            Log.d("COPY100: DIRS", dir.path)
        }

        val file = File(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL).path!!)
        Log.d("COPY100: FILE-PATH", file.path)
        Log.d("COPY100: FILE-PATH", file.exists().toString())

        val file2 = File("/storage/4329-1A0A/DCIM/Facebook/")
        val uri = Uri.fromFile(file2)
        Log.d("COPY100: URI", uri.toString())


    }
}