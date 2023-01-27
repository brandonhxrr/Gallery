package com.brandonhxrr.gallery

import android.os.Bundle
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import com.brandonhxrr.gallery.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    //private lateinit var appBarConfiguration: AppBarConfiguration
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

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/
}