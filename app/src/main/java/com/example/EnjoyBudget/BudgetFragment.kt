package com.example.EnjoyBudget



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var monthInput: EditText
    private lateinit var minGoalInput: EditText
    private lateinit var maxGoalInput: EditText
    private lateinit var saveGoalButton: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        monthInput = view.findViewById(R.id.monthInput)
        minGoalInput = view.findViewById(R.id.minGoalInput)
        maxGoalInput = view.findViewById(R.id.maxGoalInput)
        saveGoalButton = view.findViewById(R.id.saveGoalButton)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Set current month
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = dateFormat.format(Date())
        monthInput.setText(currentMonth)

        // Load existing budget goal if any
        loadBudgetGoal(currentMonth)

        // Set up save button
        saveGoalButton.setOnClickListener {
            saveBudgetGoal()
        }

        // Set up logout button
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadBudgetGoal(month: String) {
        val user = sessionManager.getUserDetails() ?: return

        val budgetGoal = databaseHelper.getBudgetGoal(month, user.id)

        if (budgetGoal != null) {
            minGoalInput.setText(budgetGoal.minGoal.toString())
            maxGoalInput.setText(budgetGoal.maxGoal.toString())
        }
    }

    private fun saveBudgetGoal() {
        val user = sessionManager.getUserDetails() ?: return

        val month = monthInput.text.toString().trim()
        val minGoalStr = minGoalInput.text.toString().trim()
        val maxGoalStr = maxGoalInput.text.toString().trim()

        if (month.isEmpty() || minGoalStr.isEmpty() || maxGoalStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val minGoal = minGoalStr.toDouble()
            val maxGoal = maxGoalStr.toDouble()

            if (minGoal > maxGoal) {
                Toast.makeText(requireContext(), "Min goal cannot be greater than max goal", Toast.LENGTH_SHORT).show()
                return
            }

            val id = databaseHelper.setBudgetGoal(month, minGoal, maxGoal, user.id)

            if (id > 0) {
                Toast.makeText(requireContext(), "Budget goal saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save budget goal", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.logout()
                startActivity(Intent(requireActivity(), MainActivity_login::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}