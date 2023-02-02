package com.brandonhxrr.gallery

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import java.io.File

open class Splash : AppCompatActivity() {

    private val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
    private val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkPermissions()

        val folders: HashMap<File, List<File>> = sortImagesByFolder(getAllImages(this)) as HashMap<File, List<File>>

        albumes = folders
        //val folders: List<File> = getAlbums(this)

        val intent = Intent(this, MainActivity::class.java)
            .apply {
            putExtra("albums", folders)
        }
        startActivity(intent)
        finish()
    }

    private fun checkPermissions() {
        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.permission_rational_read_external), REQUEST_STORAGE_READ_ACCESS_PERMISSION)
            return
        } else if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_rational_read_external), REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
            return
        } else if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {

        }
    }

    private fun hasPermission(permission: String): Boolean = ActivityCompat.checkSelfPermission(this@Splash, permission) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.title_permission_needed), rationale,
                { dialog, which ->
                    ActivityCompat.requestPermissions(this@Splash, arrayOf(permission), requestCode)
                }, getString(R.string.label_ok),
                { dialog, which ->
                    dialog.dismiss()
                    finish()
                }, getString(R.string.label_cancel))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun showAlertDialog(title: String?, message: String?,
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