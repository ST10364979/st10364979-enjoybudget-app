package com.example.EnjoyBudget


import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val IS_LOGGED_IN = "IsLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    fun createLoginSession(user: User) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, user.id)
        editor.putString(KEY_USERNAME, user.username)
        editor.putString(KEY_EMAIL, user.email)
        editor.commit()
    }

    fun getUserDetails(): User? {
        if (!isLoggedIn()) return null

        val userId = sharedPreferences.getLong(KEY_USER_ID, -1)
        val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""

        if (userId == -1L) return null

        return User(userId, username, email)
    }

    fun logout() {
        editor.clear()
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }
}