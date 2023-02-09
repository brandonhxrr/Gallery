package com.brandonhxrr.gallery

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.brandonhxrr.gallery.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val albums = albumes

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        bottomNavView = findViewById(R.id.bottomNavigationView)

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
                when(navController.currentDestination?.id) {
                    R.id.SecondFragment -> {
                        bottomNavView.selectedItemId = R.id.menu_photos
                    }
                }
                navController.navigateUp()
            }
        })
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
}