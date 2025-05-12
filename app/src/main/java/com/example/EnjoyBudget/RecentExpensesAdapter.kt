package com.example.EnjoyBudget



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

class RecentExpensesAdapter(
    private var expenses: List<Expense>
) : RecyclerView.Adapter<RecentExpensesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expenseDescription: TextView = view.findViewById(R.id.tvExpenseDescription)
        val expenseAmount: TextView = view.findViewById(R.id.tvExpenseAmount)
        val expenseDate: TextView = view.findViewById(R.id.tvExpenseDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        val currencyFormat = NumberFormat.getCurrencyInstance()

        holder.expenseDescription.text = expense.description
        holder.expenseAmount.text = currencyFormat.format(expense.amount)
        holder.expenseDate.text = expense.date
    }

    override fun getItemCount() = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}