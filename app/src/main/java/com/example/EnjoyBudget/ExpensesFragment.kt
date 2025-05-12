package com.example.EnjoyBudget


import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class ExpensesFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var dateInput: EditText
    private lateinit var startTimeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var photoPreview: ImageView
    private lateinit var selectPhotoButton: Button
    private lateinit var saveExpenseButton: Button

    private var selectedPhotoUri: Uri? = null
    private var categories = listOf<Category>()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        dateInput = view.findViewById(R.id.dateInput)
        startTimeInput = view.findViewById(R.id.startTimeInput)
        endTimeInput = view.findViewById(R.id.endTimeInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        photoPreview = view.findViewById(R.id.photoPreview)
        selectPhotoButton = view.findViewById(R.id.selectPhotoButton)
        saveExpenseButton = view.findViewById(R.id.saveExpenseButton)

        // Set current date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput.setText(dateFormat.format(calendar.time))

        // Set up date picker
        dateInput.setOnClickListener {
            showDatePicker()
        }

        // Set up time pickers
        startTimeInput.setOnClickListener {
            showTimePicker(startTimeInput)
        }

        endTimeInput.setOnClickListener {
            showTimePicker(endTimeInput)
        }

        // Set up photo selection
        selectPhotoButton.setOnClickListener {
            openGallery()
        }

        // Load categories
        loadCategories()

        // Set up save button
        saveExpenseButton.setOnClickListener {
            saveExpense()
        }
    }

    private fun loadCategories() {
        val user = sessionManager.getUserDetails() ?: return

        categories = databaseHelper.getAllCategories(user.id)

        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Please add categories first", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateInput.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun showTimePicker(timeInput: EditText) {
        val calendar = Calendar.getInstance()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeInput.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedPhotoUri = data.data
            photoPreview.setImageURI(selectedPhotoUri)
        }
    }

    private fun saveExpense() {
        val user = sessionManager.getUserDetails() ?: return

        val date = dateInput.text.toString().trim()
        val startTime = startTimeInput.text.toString().trim()
        val endTime = endTimeInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (date.isEmpty() || startTime.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (categories.isEmpty() || categorySpinner.selectedItemPosition < 0) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected category
        val selectedCategory = categories[categorySpinner.selectedItemPosition]

        // Create expense object
        val expense = Expense(
            amount = 0.0, // We'll update this with a dialog
            date = date,
            startTime = startTime,
            endTime = endTime,
            description = description,
            categoryId = selectedCategory.id,
            userId = user.id,
            photoPath = selectedPhotoUri?.toString()
        )

        // Show dialog to enter amount
        showAmountDialog(expense)
    }

    private fun showAmountDialog(expense: Expense) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_enter_amount, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()

                // Update expense with amount
                val updatedExpense = expense.copy(amount = amount)

                // Save expense to database
                val id = databaseHelper.addExpense(updatedExpense)

                if (id > 0) {
                    Toast.makeText(requireContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show()
                    clearForm()
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun clearForm() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        dateInput.setText(dateFormat.format(calendar.time))
        startTimeInput.text.clear()
        endTimeInput.text.clear()
        descriptionInput.text.clear()
        categorySpinner.setSelection(0)
        photoPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        selectedPhotoUri = null
    }
}