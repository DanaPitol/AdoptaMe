package mx.edu.unpa.adoptame.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.util.GlideImageLoader

class RegisterPhotoAdapter(
    private val onSelectPrincipal: (Int) -> Unit
) : RecyclerView.Adapter<RegisterPhotoAdapter.PhotoViewHolder>() {

    private val photos = mutableListOf<Bitmap>()
    private var principalIndex = 0

    fun addPhoto(bitmap: Bitmap) {
        photos.add(bitmap)
        if (photos.size == 1) {
            principalIndex = 0
        }
        notifyDataSetChanged()
    }

    fun getPhotos(): List<Bitmap> = photos.toList()

    fun getPrincipalIndex(): Int = principalIndex

    fun isPrincipal(index: Int): Boolean = index == principalIndex

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_register_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position], position == principalIndex) {
            principalIndex = position
            notifyDataSetChanged()
            onSelectPrincipal(position)
        }
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
        private val txtBadge: TextView = itemView.findViewById(R.id.txtPrincipalBadge)

        fun bind(bitmap: Bitmap, isPrincipal: Boolean, onSelect: () -> Unit) {
            GlideImageLoader.load(imgThumb, bitmap)
            txtBadge.visibility = if (isPrincipal) View.VISIBLE else View.GONE
            itemView.alpha = if (isPrincipal) 1f else 0.85f
            itemView.setOnClickListener { onSelect() }
        }
    }
}
