package mx.edu.unpa.adoptame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.model.PetCategory
import mx.edu.unpa.adoptame.util.GlideImageLoader

class CategoryAdapter(
    private var categories: List<PetCategory> = emptyList(),
    private val onClick: (PetCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun updateList(newCategories: List<PetCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], onClick)
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCategory: ImageView = itemView.findViewById(R.id.imgCategory)
        private val txtName: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtCategoryStatus)
        private val txtFooter: TextView = itemView.findViewById(R.id.txtCategoryFooter)

        fun bind(category: PetCategory, onClick: (PetCategory) -> Unit) {
            GlideImageLoader.load(imgCategory, category.imageUrl, category.imageRes)
            txtName.text = category.name
            txtFooter.text = category.name
            txtStatus.text = if (category.available) {
                itemView.context.getString(R.string.category_available)
            } else {
                itemView.context.getString(R.string.category_unavailable)
            }
            txtStatus.setTextColor(
                ContextCompat.getColor(itemView.context, R.color.adoptame_status_teal)
            )
            itemView.setOnClickListener { onClick(category) }
            itemView.alpha = 1f
        }
    }
}
