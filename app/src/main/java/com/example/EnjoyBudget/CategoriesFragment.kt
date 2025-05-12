package com.example.EnjoyBudget



import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesFragment : Fragment(), CategoryAdapter.CategoryClickListener {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var categoryNameInput: EditText
    private lateinit var addCategoryButton: Button
    private lateinit var categoryRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        categoryNameInput = view.findViewById(R.id.categoryNameInput)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)

        // Set up RecyclerView
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(emptyList(), this)
        categoryRecyclerView.adapter = categoryAdapter

        // Load categories
        loadCategories()

        // Set up add button
        addCategoryButton.setOnClickListener {
            addCategory()
        }
    }

    private fun loadCategories() {
        val user = sessionManager.getUserDetails() ?: return

        val categories = databaseHelper.getAllCategories(user.id)
        categoryAdapter.updateCategories(categories)
    }

    private fun addCategory() {
        val user = sessionManager.getUserDetails() ?: return

        val categoryName = categoryNameInput.text.toString().trim()

        if (categoryName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
            return
        }

        val id = databaseHelper.addCategory(categoryName, user.id)

        if (id > 0) {
            Toast.makeText(requireContext(), "Category added successfully", Toast.LENGTH_SHORT).show()
            categoryNameInput.text.clear()
            loadCategories()
        } else {
            Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCategoryDelete(category: Category) {
        // Show confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete this category?")
            .setPositiveButton("Yes") { _, _ ->
                val result = databaseHelper.deleteCategory(category.id)

                if (result > 0) {
                    Toast.makeText(requireContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}