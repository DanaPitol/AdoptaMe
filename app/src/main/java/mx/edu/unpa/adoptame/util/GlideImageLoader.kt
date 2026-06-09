package mx.edu.unpa.adoptame.util

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import mx.edu.unpa.adoptame.R

object GlideImageLoader {

    fun load(
        imageView: ImageView,
        pathOrUrl: String?,
        placeholderRes: Int = R.drawable.logo_adoptame
    ) {
        val url = ImageUrlHelper.buildUrl(pathOrUrl)
        if (url != null) {
            Glide.with(imageView.context)
                .load(url)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .into(imageView)
        } else {
            imageView.setImageResource(placeholderRes)
        }
    }

    fun load(imageView: ImageView, bitmap: Bitmap?) {
        if (bitmap != null) {
            Glide.with(imageView.context).load(bitmap).into(imageView)
        }
    }
}
