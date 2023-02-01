package com.brandonhxrr.gallery.adapter

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R
import com.brandonhxrr.gallery.CustomViewPager
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

class ViewPagerAdapter(
    val context: Context,
    private val imageList: List<Photo>
) : PagerAdapter() {

    private var hidden: Boolean = false
    private val window = (context as Activity).window
    private var toolbar: Toolbar = (context as Activity).findViewById(R.id.toolbar)
    private var bottomContainer: Toolbar  = (context as Activity).findViewById(R.id.bottom_container)
    private var constraintContainer: ConstraintLayout = (context as Activity).findViewById(R.id.constraintContainer)

    private val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val itemView: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.page, null)

        val imageView: PhotoView = itemView.findViewById(R.id.displayImage)
        val playButton: ImageView = itemView.findViewById(R.id.play_button)

        //toolbar.visibility = View.GONE
        //windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        //constraintContainer.setBackgroundColor(Color.BLACK)

        val videoExtensions = arrayOf("mp4", "mkv", "avi", "wmv", "mov")

        imageList[position].let {
            val file = File(it.path)

            if(file.extension in videoExtensions){
                playButton.visibility = View.VISIBLE

                imageView.setOnClickListener {
                    val videoUri: Uri = Uri.parse(file.path)
                    val intent = Intent(Intent.ACTION_VIEW,videoUri)
                    intent.setDataAndType(videoUri, "video/*")
                    context.startActivity(intent)
                }
            }else {
                imageView.setOnClickListener {
                    hideStatusBar()
                }
            }

            Glide.with(context)
                .load(it.path)
                .into(imageView)
        }

        (container as CustomViewPager).addView(itemView)

        return itemView
    }

    private fun hideStatusBar(){
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 200
        transition.addTarget(toolbar)

        val transitionBottomContainer: Transition = Slide(Gravity.BOTTOM)
        transitionBottomContainer.duration = 200
        transitionBottomContainer.addTarget(bottomContainer)

        TransitionManager.beginDelayedTransition(toolbar, transition)
        TransitionManager.beginDelayedTransition(bottomContainer, transitionBottomContainer)

        if(hidden){
            toolbar.visibility = View.GONE
            bottomContainer.visibility = View.GONE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            val anim = ObjectAnimator.ofArgb(constraintContainer, "backgroundColor", Color.WHITE, Color.BLACK)
            anim.duration = 200
            anim.start()
        }else {
            toolbar.visibility = View.VISIBLE
            bottomContainer.visibility = View.VISIBLE
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

            val anim = ObjectAnimator.ofArgb(constraintContainer, "backgroundColor", Color.BLACK, Color.WHITE)
            anim.duration = 200
            anim.start()
        }
        hidden = !hidden
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as CustomViewPager
        val view = `object` as View
        vp.removeView(view)
    }
}