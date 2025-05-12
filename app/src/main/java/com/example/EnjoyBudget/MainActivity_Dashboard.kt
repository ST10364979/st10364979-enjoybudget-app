package com.example.EnjoyBudget



import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity_Dashboard : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)

        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity_login::class.java))
            finish()
            return
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default fragment
        loadFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            var fragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_home -> fragment = HomeFragment()
                R.id.nav_expenses -> fragment = ExpensesFragment()
                R.id.nav_categories -> fragment = CategoriesFragment()
                R.id.nav_budget -> fragment = BudgetFragment()
            }

            if (fragment != null) {
                loadFragment(fragment)
                return@setOnItemSelectedListener true
            }

            false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, MainActivity_login::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}