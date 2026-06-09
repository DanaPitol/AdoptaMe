package mx.edu.unpa.adoptame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.model.GalleryImageItem
import mx.edu.unpa.adoptame.util.GlideImageLoader

class GalleryImageAdapter(
    private var items: List<GalleryImageItem> = emptyList(),
    private val onClick: (GalleryImageItem) -> Unit
) : RecyclerView.Adapter<GalleryImageAdapter.GalleryViewHolder>() {

    fun updateList(newItems: List<GalleryImageItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgGallery: ImageView = itemView.findViewById(R.id.imgGallery)
        private val txtPrincipalBadge: TextView = itemView.findViewById(R.id.txtPrincipalBadge)

        fun bind(item: GalleryImageItem, onClick: (GalleryImageItem) -> Unit) {
            val placeholder = item.drawableRes ?: R.drawable.imagen_gatito
            if (item.url.isNullOrBlank()) {
                imgGallery.setImageResource(placeholder)
            } else {
                GlideImageLoader.load(imgGallery, item.url, placeholder)
            }
            txtPrincipalBadge.visibility = if (item.isPrincipal) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
