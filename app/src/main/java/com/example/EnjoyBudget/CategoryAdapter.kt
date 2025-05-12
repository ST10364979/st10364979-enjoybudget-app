package com.example.EnjoyBudget



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var categories: List<Category>,
    private val listener: CategoryClickListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    interface CategoryClickListener {
        fun onCategoryDelete(category: Category)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val deleteButton: ImageButton = view.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.categoryName.text = category.name

        holder.deleteButton.setOnClickListener {
            listener.onCategoryDelete(category)
        }
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}