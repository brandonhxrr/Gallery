package com.brandonhxrr.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.brandonhxrr.gallery.adapter.view_pager.ViewPagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PhotoView : AppCompatActivity() {

    private var media: List<Photo>? = null
    private lateinit var viewPager: CustomViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var photoName: TextView
    private lateinit var photoDatetime: TextView
    private lateinit var container: ConstraintLayout
    private lateinit var toolbar: Toolbar
    private lateinit var btnDelete: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnMenu: ImageButton
    var position: Int = 0
    private lateinit var windowInsetsController : WindowInsetsControllerCompat
    private lateinit var operation: String
    private lateinit var currentFile: File
    private var deletedImageUri: Uri? = null
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val PERMISSION_PREFS_NAME = "permissions"
    private val SD_CARD_PERMISSION_GRANTED_KEY = "sd_card_permission_granted"
    private lateinit var destinationPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        setContentView(R.layout.activity_photo_view)

        photoName = findViewById(R.id.photo_name)
        photoDatetime = findViewById(R.id.photo_datetime)
        container = findViewById(R.id.constraintContainer)
        btnDelete = findViewById(R.id.btn_delete)
        btnShare = findViewById(R.id.btn_share)
        btnMenu = findViewById(R.id.btn_menu)

        toolbar = findViewById(R.id.toolbar)
        val params = toolbar.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = getStatusBarHeight()
        toolbar.layoutParams = params
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.title = ""

        val bundle = intent.extras
        position = bundle?.getInt("position")!!
        val gson = Gson()
        val data = intent.getStringExtra("data")
        media = gson.fromJson(data, Array<Photo>::class.java).toList()

        viewPager = findViewById(R.id.viewPager)
        currentFile = File(media!![position].path)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(pos: Int) {
                super.onPageSelected(pos)
                position = pos
                currentFile = File(media!![position].path)
                setDateTime()
            }
        })

        viewPagerAdapter = ViewPagerAdapter(this, media!!)
        viewPager.adapter = viewPagerAdapter
        setDateTime()
        viewPager.currentItem = position

        toolbar.visibility = View.GONE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        container.setBackgroundColor(Color.BLACK)

        btnDelete.setOnClickListener {
            showMenu(it, R.menu.menu_delete)
        }

        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = if (imageExtensions.contains(currentFile.extension.lowercase())) "image/*" else "video/*"
            val uri = FileProvider.getUriForFile(this, "${this.packageName}.provider", currentFile)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, getString(R.string.menu_share)))
        }

        btnMenu.setOnClickListener {
            showSubmenu(it, R.menu.menu_submenu)
        }

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == RESULT_OK) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deletePhotoFromExternal(this@PhotoView, deletedImageUri ?: return@launch, intentSenderLauncher)
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.file_not_deleted), Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics
                    )
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon =
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        if(currentFile.delete()){
                            removeImageFromAdapter()
                            Toast.makeText(this, getString(R.string.file_deleted), Toast.LENGTH_SHORT).show()
                        }else if(deletePhotoFromExternal(this, getContentUri(this, currentFile)!!, intentSenderLauncher)) {
                           removeImageFromAdapter()
                            Toast.makeText(this, getString(R.string.file_deleted), Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(this, getString(R.string.file_not_deleted), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true
            }
            popup.setOnDismissListener {
            }
            popup.show()
        }
    }

    private fun removeImageFromAdapter(){
        media = ArrayList(media!!).apply { removeAt(position) }

        if((media as ArrayList<Photo>).isNotEmpty()){
            viewPagerAdapter.updateData(media!!)
            viewPager.adapter = viewPagerAdapter
            viewPager.invalidate()
            viewPager.currentItem = position
            currentFile = File(media!![position].path)
            setDateTime()
        }else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showSubmenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_details-> {
                    //"dd/MM/yyyy HH:mm a"
                    val dateFormat = SimpleDateFormat(getString(R.string.time_format), Locale.getDefault())
                    val lastModified = currentFile.lastModified()
                    val fileSize = currentFile.length()

                    val fileSizeString: String = if (fileSize >= 1024 * 1024 * 1024) {
                        String.format(Locale.getDefault(), "%.2f GB", fileSize.toFloat() / (1024 * 1024 * 1024))
                    } else if (fileSize >= 1024 * 1024) {
                        String.format(Locale.getDefault(), "%.2f MB", fileSize.toFloat() / (1024 * 1024))
                    } else if (fileSize >= 1024) {
                        String.format(Locale.getDefault(), "%.2f KB", fileSize.toFloat() / 1024)
                    } else {
                        "$fileSize bytes"
                    }

                    MaterialAlertDialogBuilder(this)
                        //.setTitle((position + 1).toString() + "/" + media!!.size.toString())
                        .setMessage(getString(R.string.details_path) + ": " + currentFile.absolutePath
                                + "\n" + getString(R.string.details_type) + ": " + currentFile.extension
                                + "\n" + getString(R.string.details_size) + ": " + fileSizeString
                                + "\n" + getString(R.string.details_resolution) + ": " + getResolution(currentFile.path)
                                + "\n" + getString(R.string.details_date) + ": " + dateFormat.format(Date(lastModified)))
                        .setPositiveButton(getString(R.string.accept)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                R.id.menu_move -> {
                    val selectionIntent = Intent(this, AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "MOVE"
                }
                R.id.menu_copy -> {

                    val mimeType: String = if (currentFile.extension in imageExtensions) "image/${currentFile.extension}" else "video/${currentFile.extension}"

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .setType(mimeType)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setComponent(ComponentName(this, AlbumSelection::class.java))
                    resultLauncher.launch(intent)
                    operation = "COPY"
                }
                R.id.menu_rename -> {

                    val view = layoutInflater.inflate(R.layout.alert_edit_text, null)
                    val textInputLayout = view.findViewById<TextInputLayout>(R.id.text_input_layout)
                    val textInputEditText = view.findViewById<TextInputEditText>(R.id.text_input_edit_text)

                    textInputEditText.setText(currentFile.nameWithoutExtension)

                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.menu_rename))
                        .setView(textInputLayout)
                        .setPositiveButton(getString(R.string.menu_rename)) { _, _ ->
                            var newName = textInputEditText.text.toString()
                            newName += "." + currentFile.extension
                            val newFile = File(currentFile.parent!! + "/" + newName)
                            if (currentFile.renameTo(newFile)) {
                                currentFile = File(media!![position].path)
                                Toast.makeText(this, getString(R.string.file_renamed), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, getString(R.string.file_not_renamed), Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            true
        }
        popup.setOnDismissListener {}
        popup.show()
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            destinationPath = data?.getStringExtra("RUTA")!!

            when(operation) {
                "MOVE" -> {
                    copyFileToUri(this, currentFile, destinationPath, true, requestPermissionLauncher, intentSenderLauncher)
                    removeImageFromAdapter()
                }
                "COPY" -> {
                    copyFileToUri(this, currentFile, destinationPath, false, requestPermissionLauncher, intentSenderLauncher)
                }
            }
        }
    }

    private fun getResolution(path: String): String {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        return options.outWidth.toString() + "x" + options.outHeight.toString()
    }

    private fun setDateTime() {
        val date = Date(currentFile.lastModified())
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault())
        val outputFormatDate = SimpleDateFormat(getString(R.string.time_format), Locale.getDefault())

        val input = inputFormat.format(date)
        val dateParse = inputFormat.parse(input)

        photoName.text = currentFile.name

        photoDatetime.text = outputFormatDate.format(dateParse!!)
    }

    @SuppressLint("InternalInsetResource")
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                val sharedPreferences = getSharedPreferences(PERMISSION_PREFS_NAME, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean(SD_CARD_PERMISSION_GRANTED_KEY, true)
                editor.apply()
                copyToExternal(this, currentFile, destinationPath, operation == "MOVE", intentSenderLauncher)
            }
        }
    }
}