package com.brandonhxrr.gallery.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.brandonhxrr.gallery.Photo
import com.brandonhxrr.gallery.R
import com.bumptech.glide.Glide
import java.io.File

class ViewPagerAdapter(
    val context: Context,
    private val imageList: List<Photo>
) : PagerAdapter() {
    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val itemView: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.page, null)

        val imageView: ImageView = itemView.findViewById(R.id.displayImage)
        val playButton: ImageView = itemView.findViewById(R.id.play_button)

        val videoExtensions = arrayOf("mp4", "mkv", "avi", "wmv", "mov")

        imageList[position].let {
            val file = File(it.path)

            //fileTitle.text = if(position == 0) File(imageList[position].path).name else File(imageList[position-1].path).name

            if(file.extension in videoExtensions){
                playButton.visibility = View.VISIBLE

                imageView.setOnClickListener {
                    val videoUri: Uri = Uri.parse(file.path)
                    val intent = Intent(Intent.ACTION_VIEW,videoUri)
                    intent.setDataAndType(videoUri, "video/*")
                    context.startActivity(intent)
                }
            }

            Glide.with(context)
                .load(it.path)
                .into(imageView)
        }

        val vp = container as ViewPager
        vp.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }
}