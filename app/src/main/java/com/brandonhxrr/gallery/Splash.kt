package com.brandonhxrr.gallery

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

open class Splash : AppCompatActivity() {
    private val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
    private val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.permission_rational_read_external), REQUEST_STORAGE_READ_ACCESS_PERMISSION)
        } else if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_rational_read_external), REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
        } else {
            loadData()
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(this@Splash, permission) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_STORAGE_READ_ACCESS_PERMISSION, REQUEST_STORAGE_WRITE_ACCESS_PERMISSION -> loadData()
            }
        } else {
            showAlertDialog(
                title = "Permission denied",
                message = "Permission to access storage is required to continue",
                onPositiveButtonClickListener = { _, _ -> checkPermissions() },
                positiveText = "Retry",
                onNegativeButtonClickListener = { _, _ -> finish() },
                negativeText = "Exit"
            )
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

    private fun showAlertDialog(title: String?, message: String?,
                                onPositiveButtonClickListener: DialogInterface.OnClickListener?,
                                positiveText: String,
                                onNegativeButtonClickListener: DialogInterface.OnClickListener?,
                                negativeText: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, onPositiveButtonClickListener)
            .setNegativeButton(negativeText, onNegativeButtonClickListener)
            .setCancelable(false)
            .show()
    }
}