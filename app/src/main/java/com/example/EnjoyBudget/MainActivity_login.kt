package com.example.EnjoyBudget





import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity_login : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        val editTextEmail = findViewById<EditText>(R.id.editTextText2)
        val editTextPassword = findViewById<EditText>(R.id.editTextTextPassword4)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnReset = findViewById<Button>(R.id.buttonRest)

        btnLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = databaseHelper.checkUser(email, password)

            if (user != null) {
                sessionManager.createLoginSession(user)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity_Dashboard::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnReset.setOnClickListener {
            editTextEmail.text.clear()
            editTextPassword.text.clear()
        }
    }
}