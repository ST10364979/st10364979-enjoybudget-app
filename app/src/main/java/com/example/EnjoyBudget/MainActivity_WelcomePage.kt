package com.example.EnjoyBudget



import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity_WelcomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_welcome_page)

        val btnContinue = findViewById<Button>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            val sessionManager = SessionManager(this)

            if (sessionManager.isLoggedIn()) {
                // User is already logged in, go to main dashboard
                startActivity(Intent(this, MainActivity_Dashboard::class.java))
            } else {
                // User is not logged in, go to login page
                startActivity(Intent(this, MainActivity_login::class.java))
            }
            finish()
        }
    }
}