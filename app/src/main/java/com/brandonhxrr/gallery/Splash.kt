package com.brandonhxrr.gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

open class Splash : AppCompatActivity() {
    private val REQUEST_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) || !hasPermission(Manifest.permission.MANAGE_DOCUMENTS)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_DOCUMENTS), REQUEST_PERMISSIONS)
        }else {
            loadData()
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_PERMISSIONS -> loadData()
            }
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission denied")
                .setMessage("Permission to access storage is required to continue")
                .setPositiveButton("Retry"){ _, _ ->
                    checkPermissions()
                }.setNegativeButton("Exit"){ _, _ ->
                    finish()
                }.setCancelable(false).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun loadData() {
        val folders: HashMap<File, List<File>> = sortImagesByFolder(getAllImages(this)) as HashMap<File, List<File>>
        albumes = folders

        val intent = Intent(this, MainActivity::class.java)
            .apply {
                putExtra("albums", folders)
            }
        startActivity(intent)
        finish()
    }
}