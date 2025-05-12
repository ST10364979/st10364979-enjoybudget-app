package com.example.EnjoyBudget


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "goodbudget.db"
        private const val DATABASE_VERSION = 1

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_USERNAME = "username"
        private const val COLUMN_USER_PASSWORD = "password"
        private const val COLUMN_USER_EMAIL = "email"

        // Categories table
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CATEGORY_ID = "id"
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_USER_ID = "user_id"

        // Expenses table
        private const val TABLE_EXPENSES = "expenses"
        private const val COLUMN_EXPENSE_ID = "id"
        private const val COLUMN_EXPENSE_AMOUNT = "amount"
        private const val COLUMN_EXPENSE_DATE = "date"
        private const val COLUMN_EXPENSE_START_TIME = "start_time"
        private const val COLUMN_EXPENSE_END_TIME = "end_time"
        private const val COLUMN_EXPENSE_DESCRIPTION = "description"
        private const val COLUMN_EXPENSE_CATEGORY_ID = "category_id"
        private const val COLUMN_EXPENSE_USER_ID = "user_id"
        private const val COLUMN_EXPENSE_PHOTO_PATH = "photo_path"

        // Budget Goals table
        private const val TABLE_BUDGET_GOALS = "budget_goals"
        private const val COLUMN_GOAL_ID = "id"
        private const val COLUMN_GOAL_MONTH = "month"
        private const val COLUMN_GOAL_MIN = "min_goal"
        private const val COLUMN_GOAL_MAX = "max_goal"
        private const val COLUMN_GOAL_USER_ID = "user_id"

        // Income table
        private const val TABLE_INCOME = "income"
        private const val COLUMN_INCOME_ID = "id"
        private const val COLUMN_INCOME_AMOUNT = "amount"
        private const val COLUMN_INCOME_SOURCE = "source"
        private const val COLUMN_INCOME_DATE = "date"
        private const val COLUMN_INCOME_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT NOT NULL UNIQUE
            )
        """)

        // Create Categories table
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL,
                $COLUMN_CATEGORY_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_CATEGORY_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)

        // Create Expenses table
        db.execSQL("""
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXPENSE_AMOUNT REAL NOT NULL,
                $COLUMN_EXPENSE_DATE TEXT NOT NULL,
                $COLUMN_EXPENSE_START_TIME TEXT,
                $COLUMN_EXPENSE_END_TIME TEXT,
                $COLUMN_EXPENSE_DESCRIPTION TEXT,
                $COLUMN_EXPENSE_CATEGORY_ID INTEGER NOT NULL,
                $COLUMN_EXPENSE_USER_ID INTEGER NOT NULL,
                $COLUMN_EXPENSE_PHOTO_PATH TEXT,
                FOREIGN KEY ($COLUMN_EXPENSE_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID),
                FOREIGN KEY ($COLUMN_EXPENSE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)

        // Create Budget Goals table
        db.execSQL("""
            CREATE TABLE $TABLE_BUDGET_GOALS (
                $COLUMN_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_GOAL_MONTH TEXT NOT NULL,
                $COLUMN_GOAL_MIN REAL NOT NULL,
                $COLUMN_GOAL_MAX REAL NOT NULL,
                $COLUMN_GOAL_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_GOAL_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)

        // Create Income table
        db.execSQL("""
            CREATE TABLE $TABLE_INCOME (
                $COLUMN_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_INCOME_AMOUNT REAL NOT NULL,
                $COLUMN_INCOME_SOURCE TEXT NOT NULL,
                $COLUMN_INCOME_DATE TEXT NOT NULL,
                $COLUMN_INCOME_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_INCOME_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

        // Create tables again
        onCreate(db)
    }

    // User methods
    fun registerUser(username: String, password: String, email: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_USERNAME, username)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_EMAIL, email)
        }

        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun checkUser(email: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USER_USERNAME, COLUMN_USER_EMAIL),
            "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
            user = User(id, username, userEmail)
        }

        cursor.close()
        db.close()
        return user
    }

    fun checkUserExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Category methods
    fun addCategory(name: String, userId: Long): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, name)
            put(COLUMN_CATEGORY_USER_ID, userId)
        }

        val id = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return id
    }

    fun getAllCategories(userId: Long): List<Category> {
        val categories = mutableListOf<Category>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COLUMN_CATEGORY_ID, COLUMN_CATEGORY_NAME),
            "$COLUMN_CATEGORY_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$COLUMN_CATEGORY_NAME ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                categories.add(Category(id, name, userId))
            }
        }

        cursor.close()
        db.close()
        return categories
    }

    fun deleteCategory(categoryId: Long): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_CATEGORIES,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )
        db.close()
        return result
    }

    // Expense methods
    fun addExpense(expense: Expense): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXPENSE_AMOUNT, expense.amount)
            put(COLUMN_EXPENSE_DATE, expense.date)
            put(COLUMN_EXPENSE_START_TIME, expense.startTime)
            put(COLUMN_EXPENSE_END_TIME, expense.endTime)
            put(COLUMN_EXPENSE_DESCRIPTION, expense.description)
            put(COLUMN_EXPENSE_CATEGORY_ID, expense.categoryId)
            put(COLUMN_EXPENSE_USER_ID, expense.userId)
            put(COLUMN_EXPENSE_PHOTO_PATH, expense.photoPath)
        }

        val id = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return id
    }

    fun getAllExpenses(userId: Long): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_EXPENSE_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$COLUMN_EXPENSE_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT))
                val date = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_DATE))
                val startTime = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_START_TIME))
                val endTime = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_END_TIME))
                val description = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_DESCRIPTION))
                val categoryId = getLong(getColumnIndexOrThrow(COLUMN_EXPENSE_CATEGORY_ID))
                val photoPath = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_PHOTO_PATH))

                expenses.add(Expense(id, amount, date, startTime, endTime, description, categoryId, userId, photoPath))
            }
        }

        cursor.close()
        db.close()
        return expenses
    }

    fun getExpensesByCategory(userId: Long, categoryId: Long): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_EXPENSE_USER_ID = ? AND $COLUMN_EXPENSE_CATEGORY_ID = ?",
            arrayOf(userId.toString(), categoryId.toString()),
            null, null, "$COLUMN_EXPENSE_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT))
                val date = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_DATE))
                val startTime = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_START_TIME))
                val endTime = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_END_TIME))
                val description = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_DESCRIPTION))
                val photoPath = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_PHOTO_PATH))

                expenses.add(Expense(id, amount, date, startTime, endTime, description, categoryId, userId, photoPath))
            }
        }

        cursor.close()
        db.close()
        return expenses
    }

    fun getExpensesByMonth(userId: Long, month: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_EXPENSE_USER_ID = ? AND $COLUMN_EXPENSE_DATE LIKE ?",
            arrayOf(userId.toString(), "$month%"),
            null, null, "$COLUMN_EXPENSE_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT))
                val date = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_DATE))
                val startTime = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_START_TIME))
                val endTime = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_END_TIME))
                val description = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_DESCRIPTION))
                val categoryId = getLong(getColumnIndexOrThrow(COLUMN_EXPENSE_CATEGORY_ID))
                val photoPath = getString(getColumnIndexOrThrow(COLUMN_EXPENSE_PHOTO_PATH))

                expenses.add(Expense(id, amount, date, startTime, endTime, description, categoryId, userId, photoPath))
            }
        }

        cursor.close()
        db.close()
        return expenses
    }

    fun getTotalExpensesByMonth(userId: Long, month: String): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_EXPENSE_AMOUNT) FROM $TABLE_EXPENSES WHERE $COLUMN_EXPENSE_USER_ID = ? AND $COLUMN_EXPENSE_DATE LIKE ?",
            arrayOf(userId.toString(), "$month%")
        )

        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        db.close()
        return total
    }

    fun getTotalExpensesByCategory(userId: Long): List<CategorySummary> {
        val categorySummaries = mutableListOf<CategorySummary>()
        val db = this.readableDatabase

        // Get current month
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = dateFormat.format(Date())

        // Get all categories
        val categories = getAllCategories(userId)

        for (category in categories) {
            val cursor = db.rawQuery(
                """
                SELECT SUM($COLUMN_EXPENSE_AMOUNT) 
                FROM $TABLE_EXPENSES 
                WHERE $COLUMN_EXPENSE_USER_ID = ? 
                AND $COLUMN_EXPENSE_CATEGORY_ID = ? 
                AND $COLUMN_EXPENSE_DATE LIKE ?
                """,
                arrayOf(userId.toString(), category.id.toString(), "$currentMonth%")
            )

            var totalSpent = 0.0
            if (cursor.moveToFirst()) {
                totalSpent = cursor.getDouble(0)
            }

            // Get budget for this category if exists
            val budgetCursor = db.rawQuery(
                """
                SELECT $COLUMN_GOAL_MAX 
                FROM $TABLE_BUDGET_GOALS 
                WHERE $COLUMN_GOAL_USER_ID = ? 
                AND $COLUMN_GOAL_MONTH = ?
                """,
                arrayOf(userId.toString(), currentMonth)
            )

            var budget = 0.0
            if (budgetCursor.moveToFirst()) {
                budget = budgetCursor.getDouble(0)
            }

            categorySummaries.add(CategorySummary(category.id, category.name, totalSpent, budget))

            cursor.close()
            budgetCursor.close()
        }

        db.close()
        return categorySummaries
    }

    // Budget Goal methods
    fun setBudgetGoal(month: String, minGoal: Double, maxGoal: Double, userId: Long): Long {
        val db = this.writableDatabase

        // Check if a goal for this month already exists
        val cursor = db.query(
            TABLE_BUDGET_GOALS,
            arrayOf(COLUMN_GOAL_ID),
            "$COLUMN_GOAL_MONTH = ? AND $COLUMN_GOAL_USER_ID = ?",
            arrayOf(month, userId.toString()),
            null, null, null
        )

        val values = ContentValues().apply {
            put(COLUMN_GOAL_MONTH, month)
            put(COLUMN_GOAL_MIN, minGoal)
            put(COLUMN_GOAL_MAX, maxGoal)
            put(COLUMN_GOAL_USER_ID, userId)
        }

        var id: Long

        if (cursor.moveToFirst()) {
            // Update existing goal
            val goalId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID))
            db.update(
                TABLE_BUDGET_GOALS,
                values,
                "$COLUMN_GOAL_ID = ?",
                arrayOf(goalId.toString())
            )
            id = goalId
        } else {
            // Insert new goal
            id = db.insert(TABLE_BUDGET_GOALS, null, values)
        }

        cursor.close()
        db.close()
        return id
    }

    fun getBudgetGoal(month: String, userId: Long): BudgetGoal? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_BUDGET_GOALS,
            null,
            "$COLUMN_GOAL_MONTH = ? AND $COLUMN_GOAL_USER_ID = ?",
            arrayOf(month, userId.toString()),
            null, null, null
        )

        var budgetGoal: BudgetGoal? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID))
            val minGoal = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_MIN))
            val maxGoal = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_MAX))

            budgetGoal = BudgetGoal(id, month, minGoal, maxGoal, userId)
        }

        cursor.close()
        db.close()
        return budgetGoal
    }

    // Income methods
    fun addIncome(income: Income): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INCOME_AMOUNT, income.amount)
            put(COLUMN_INCOME_SOURCE, income.source)
            put(COLUMN_INCOME_DATE, income.date)
            put(COLUMN_INCOME_USER_ID, income.userId)
        }

        val id = db.insert(TABLE_INCOME, null, values)
        db.close()
        return id
    }

    fun getAllIncome(userId: Long): List<Income> {
        val incomeList = mutableListOf<Income>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_INCOME,
            null,
            "$COLUMN_INCOME_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$COLUMN_INCOME_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_INCOME_ID))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_INCOME_AMOUNT))
                val source = getString(getColumnIndexOrThrow(COLUMN_INCOME_SOURCE))
                val date = getString(getColumnIndexOrThrow(COLUMN_INCOME_DATE))

                incomeList.add(Income(id, amount, source, date, userId))
            }
        }

        cursor.close()
        db.close()
        return incomeList
    }

    fun getTotalIncomeByMonth(userId: Long, month: String): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_INCOME_AMOUNT) FROM $TABLE_INCOME WHERE $COLUMN_INCOME_USER_ID = ? AND $COLUMN_INCOME_DATE LIKE ?",
            arrayOf(userId.toString(), "$month%")
        )

        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        db.close()
        return total
    }
}