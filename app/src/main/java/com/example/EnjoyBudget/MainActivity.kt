package com.example.EnjoyBudget





import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)

        val editTextUsername = findViewById<EditText>(R.id.editTextText)
        val editTextPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.editTextTextPassword2)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmali2)
        val btnSubmit = findViewById<Button>(R.id.buttonSubmit)
        val btnReset = findViewById<Button>(R.id.buttonRest)

        btnSubmit.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()
            val email = editTextEmail.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.checkUserExists(email)) {
                Toast.makeText(this, "User already exists with this email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = databaseHelper.registerUser(username, password, email)

            if (id > 0) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity_login::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }

        btnReset.setOnClickListener {
            editTextUsername.text.clear()
            editTextPassword.text.clear()
            editTextConfirmPassword.text.clear()
            editTextEmail.text.clear()
        }
    }
}