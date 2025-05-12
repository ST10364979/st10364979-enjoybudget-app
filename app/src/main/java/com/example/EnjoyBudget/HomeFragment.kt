package com.example.EnjoyBudget



import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var recentExpensesAdapter: RecentExpensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvTotalBudget = view.findViewById<TextView>(R.id.tvTotalBudget)
        val tvTotalSpent = view.findViewById<TextView>(R.id.tvTotalSpent)
        val tvRemainingBudget = view.findViewById<TextView>(R.id.tvRemainingBudget)
        val rvRecentExpenses = view.findViewById<RecyclerView>(R.id.rvRecentExpenses)
        val btnIncome = view.findViewById<Button>(R.id.btnIncome)
        val btnOutcome = view.findViewById<Button>(R.id.btnOutcome)

        // Set up RecyclerView
        rvRecentExpenses.layoutManager = LinearLayoutManager(requireContext())
        recentExpensesAdapter = RecentExpensesAdapter(emptyList())
        rvRecentExpenses.adapter = recentExpensesAdapter

        // Get user details
        val user = sessionManager.getUserDetails()

        if (user != null) {
            tvWelcome.text = "Welcome, ${user.username}!"

            // Get current month
            val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val currentMonth = dateFormat.format(Date())

            // Get budget goal for current month
            val budgetGoal = databaseHelper.getBudgetGoal(currentMonth, user.id)

            // Get total expenses for current month
            val totalExpenses = databaseHelper.getTotalExpensesByMonth(user.id, currentMonth)

            // Get total income for current month
            val totalIncome = databaseHelper.getTotalIncomeByMonth(user.id, currentMonth)

            // Format currency
            val currencyFormat = NumberFormat.getCurrencyInstance()

            // Set budget information
            val maxBudget = budgetGoal?.maxGoal ?: 0.0
            tvTotalBudget.text = "Budget: ${currencyFormat.format(maxBudget)}"
            tvTotalSpent.text = "Spent: ${currencyFormat.format(totalExpenses)}"
            tvRemainingBudget.text = "Remaining: ${currencyFormat.format(maxBudget - totalExpenses)}"

            // Get recent expenses
            val recentExpenses = databaseHelper.getAllExpenses(user.id).take(5)
            recentExpensesAdapter.updateExpenses(recentExpenses)
        }

        btnIncome.setOnClickListener {
            showAddIncomeDialog()
        }

        btnOutcome.setOnClickListener {
            // Navigate to expenses fragment
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, ExpensesFragment())
            transaction.commit()
        }
    }

    private fun showAddIncomeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etIncomeAmount)
        val etSource = dialogView.findViewById<EditText>(R.id.etIncomeSource)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelIncome)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveIncome)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val source = etSource.text.toString().trim()

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (source.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a source", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val user = sessionManager.getUserDetails()

                if (user != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    val income = Income(
                        amount = amount,
                        source = source,
                        date = currentDate,
                        userId = user.id
                    )

                    val id = databaseHelper.addIncome(income)

                    if (id > 0) {
                        Toast.makeText(requireContext(), "Income added successfully", Toast.LENGTH_SHORT).show()
                        // Refresh data
                        onViewCreated(requireView(), null)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add income", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}