package com.brandonhxrr.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import androidx.viewpager.widget.ViewPager
import com.brandonhxrr.gallery.adapter.ViewPagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoView : AppCompatActivity() {

    private var media: List<Photo>? = null
    private lateinit var viewPager: CustomViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var photoDate: TextView
    private lateinit var photoTime: TextView
    private lateinit var container: ConstraintLayout
    private lateinit var bottomContainer: Toolbar
    private lateinit var toolbar: Toolbar
    private lateinit var btnDelete: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnMenu: ImageButton
    var position: Int = 0
    private lateinit var windowInsetsController : WindowInsetsControllerCompat
    private lateinit var operation: String
    private lateinit var currentFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContentView(R.layout.activity_photo_view)

        photoDate = findViewById(R.id.photo_date)
        photoTime = findViewById(R.id.photo_time)
        container = findViewById(R.id.constraintContainer)

        bottomContainer = findViewById(R.id.bottom_container)
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
        val path = bundle?.getString("path")
        position = bundle?.getInt("position")!!
        val gson = Gson()
        val data = intent.getStringExtra("data")
        media = gson.fromJson(data, Array<Photo>::class.java).toList()

        viewPager = findViewById(R.id.viewPager)
        currentFile = File(media!![position].path)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(pos: Int) {
                super.onPageSelected(pos)
                setDateTime(pos)
                position = pos
                currentFile = File(media!![position].path)
            }
        })

        viewPagerAdapter = ViewPagerAdapter(this, media!!)
        viewPager.adapter = viewPagerAdapter
        setDateTime(position)
        viewPager.currentItem = position

        toolbar.visibility = View.GONE
        bottomContainer.visibility = View.GONE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        container.setBackgroundColor(Color.BLACK)

        btnDelete.setOnClickListener {
            showMenu(it, R.menu.menu_delete)
        }

        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = if (imageExtensions.contains(currentFile.extension)) "image/*" else "video/*"
            val uri = FileProvider.getUriForFile(this, "${this.packageName}.provider", currentFile)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, "Share"))
        }

        btnMenu.setOnClickListener {
            showSubmenu(it, R.menu.menu_submenu)
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
                // Respond to menu item click.
                when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        currentFile.delete()
                        media = ArrayList(media!!).apply { removeAt(position) }

                        if((media as ArrayList<Photo>).isNotEmpty()){
                            viewPagerAdapter.updateData(media!!)
                            viewPager.adapter = viewPagerAdapter
                            viewPager.invalidate()
                            setDateTime(position)
                            viewPager.currentItem = position
                            currentFile = File(media!![position].path)
                        }else {
                            this.onBackPressed()
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
    private fun showSubmenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when (menuItem.itemId) {
                R.id.menu_details-> {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm a", Locale.getDefault())
                    val lastModified = currentFile.lastModified()
                    val fileSize = currentFile.length()
                    val fileSizeString: String

                    if (fileSize >= 1024 * 1024 * 1024) {
                        fileSizeString = String.format(Locale.getDefault(), "%.2f GB", fileSize.toFloat() / (1024 * 1024 * 1024))
                    } else if (fileSize >= 1024 * 1024) {
                        fileSizeString = String.format(Locale.getDefault(), "%.2f MB", fileSize.toFloat() / (1024 * 1024))
                    } else if (fileSize >= 1024) {
                        fileSizeString = String.format(Locale.getDefault(), "%.2f KB", fileSize.toFloat() / 1024)
                    } else {
                        fileSizeString = "$fileSize bytes"
                    }

                    MaterialAlertDialogBuilder(this)
                        //.setTitle((position + 1).toString() + "/" + media!!.size.toString())
                        .setMessage("Ruta: " + currentFile.absolutePath
                                + "\nTipo: " + currentFile.extension
                                + "\nTamaño: " + fileSizeString
                                + "\nResolución: " + getResolution(currentFile.path)
                                + "\nFecha: " + dateFormat.format(Date(lastModified)))
                        .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, _ ->
                            dialog.dismiss()
                        })
                        .show()

                }
                R.id.menu_move -> {
                    val selectionIntent = Intent(this, AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "MOVE"
                }
                R.id.menu_copy -> {
                    val selectionIntent = Intent(this, AlbumSelection::class.java)
                    resultLauncher.launch(selectionIntent)
                    operation = "COPY"
                }
                R.id.menu_rename -> {

                    val view = layoutInflater.inflate(R.layout.alert_edit_text, null)
                    val textInputLayout = view.findViewById<TextInputLayout>(R.id.text_input_layout)
                    val textInputEditText = view.findViewById<TextInputEditText>(R.id.text_input_edit_text)

                    textInputEditText.setText(currentFile.nameWithoutExtension)

                    MaterialAlertDialogBuilder(this)
                        .setTitle("Renombrar")
                        .setView(textInputLayout)
                        .setPositiveButton("Renombrar") { _, _ ->
                            var newName = textInputEditText.text.toString()
                            newName += "." + currentFile.extension
                            val newFile = File(currentFile.parent + "/" + newName)
                            if (currentFile.renameTo(newFile)) {
                                currentFile = File(media!![position].path)
                                Toast.makeText(this, "El archivo se ha renombrado correctamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "No se pudo renombrar el archivo", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar") { dialog, _ ->
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
            val ruta: String = data?.getStringExtra("RUTA")!!
            Toast.makeText(this, ruta, Toast.LENGTH_SHORT).show()

            when(operation) {
                "MOVE" -> {

                }
                "COPY" -> {

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

    private fun setDateTime(position : Int) {
        val date = Date(currentFile.lastModified())
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault())
        val outputFormatDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
        val outputFormatTime = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val input = inputFormat.format(date)
        val dateParse = inputFormat.parse(input)

        photoDate.text = outputFormatDate.format(dateParse!!)
        photoTime.text = outputFormatTime.format(dateParse)
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}