package com.example.EnjoyBudget


data class Expense(
    val id: Long = 0,
    val amount: Double,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val categoryId: Long,
    val userId: Long,
    val photoPath: String? = null
)