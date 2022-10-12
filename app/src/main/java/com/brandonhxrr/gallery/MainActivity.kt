package com.brandonhxrr.gallery

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.brandonhxrr.gallery.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView: BottomNavigationView

    protected val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
    protected val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        checkPermissions()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        bottomNavView = findViewById(R.id.bottomNavigationView)

        bottomNavView.setOnItemSelectedListener {
            menuItem ->
            when(menuItem.itemId) {
                R.id.menu_photos -> {
                    if (navController.currentDestination?.id != R.id.FirstFragment){
                        navController.navigate(R.id.action_SecondFragment_to_FirstFragment)
                    }
                    true
                }

                R.id.menu_album -> {
                    if (navController.currentDestination?.id != R.id.SecondFragment){
                        navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }
                    true
                }

                else -> false
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkPermissions() {
        if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            requestPermission(READ_EXTERNAL_STORAGE, getString(R.string.permission_rational_read_external), REQUEST_STORAGE_READ_ACCESS_PERMISSION)
            return
        } else if (!hasPermission(WRITE_EXTERNAL_STORAGE)) {
            requestPermission(WRITE_EXTERNAL_STORAGE, getString(R.string.permission_rational_read_external), REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
            return
        } else if (hasPermission(READ_EXTERNAL_STORAGE) && hasPermission(WRITE_EXTERNAL_STORAGE)) {

        }
    }

    fun hasPermission(permission: String): Boolean = ActivityCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.title_permission_needed), rationale,
                { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
                }, getString(R.string.label_ok),
                { dialog, which ->
                    dialog.dismiss()
                    finish()
                }, getString(R.string.label_cancel))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    protected fun showAlertDialog(title: String?, message: String?,
                                  onPositiveButtonClickListener: DialogInterface.OnClickListener?,
                                  positiveText: String,
                                  onNegativeButtonClickListener: DialogInterface.OnClickListener?,
                                  negativeText: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, onPositiveButtonClickListener)
            .setNegativeButton(negativeText, onNegativeButtonClickListener)
            .setCancelable(false)
            .show()
    }

}