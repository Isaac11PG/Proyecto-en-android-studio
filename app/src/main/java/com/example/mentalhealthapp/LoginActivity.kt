package com.example.mentalhealthapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import com.example.mentalhealthapp.database.entities.User
import com.example.mentalhealthapp.database.entities.UserStats
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var rootView: android.view.View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        rootView = findViewById(android.R.id.content)
        database = AppDatabase.getDatabase(this)

        // Verificar si ya hay sesión activa
        val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            goToMainActivity()
            return
        }

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val textViewRegister = findViewById<TextView>(R.id.textViewRegister)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(rootView, "Por favor completa todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Login usando Room
            lifecycleScope.launch {
                val user = database.userDao().login(email, password)

                if (user != null) {
                    // Guardar sesión
                    sharedPreferences.edit().apply {
                        putInt("current_user_id", user.id)
                        putString("user_name", user.name)
                        putString("user_email", user.email)
                        putBoolean("is_logged_in", true)
                        apply()
                    }

                    Snackbar.make(rootView, "¡Bienvenido de vuelta, ${user.name}!", Snackbar.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Snackbar.make(rootView, "Email o contraseña incorrectos", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        textViewRegister.setOnClickListener {
            showRegisterDialog()
        }
    }

    private fun showRegisterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register, null)
        val editName = dialogView.findViewById<EditText>(R.id.editRegisterName)
        val editEmail = dialogView.findViewById<EditText>(R.id.editRegisterEmail)
        val editPassword = dialogView.findViewById<EditText>(R.id.editRegisterPassword)
        val editConfirmPassword = dialogView.findViewById<EditText>(R.id.editRegisterConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Crear cuenta")
            .setView(dialogView)
            .setPositiveButton("Registrar") { _, _ ->
                val name = editName.text.toString().trim()
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString().trim()
                val confirmPassword = editConfirmPassword.text.toString().trim()

                when {
                    name.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                        Snackbar.make(rootView, "Completa todos los campos", Snackbar.LENGTH_SHORT).show()
                    }
                    password != confirmPassword -> {
                        Snackbar.make(rootView, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show()
                    }
                    password.length < 6 -> {
                        Snackbar.make(rootView, "La contraseña debe tener al menos 6 caracteres", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {
                        registerUser(name, email, password)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun registerUser(name: String, email: String, password: String) {
        lifecycleScope.launch {
            // Verificar si el email ya existe
            val existingUser = database.userDao().getUserByEmail(email)

            if (existingUser != null) {
                Snackbar.make(rootView, "Este email ya está registrado", Snackbar.LENGTH_LONG).show()
                return@launch
            }

            // Crear nuevo usuario
            val newUser = User(
                name = name,
                email = email,
                password = password
            )

            val userId = database.userDao().insertUser(newUser).toInt()

            // Crear estadísticas iniciales para el usuario
            val initialStats = UserStats(userId = userId)
            database.userStatsDao().insertStats(initialStats)

            // Guardar sesión
            val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putInt("current_user_id", userId)
                putString("user_name", name)
                putString("user_email", email)
                putBoolean("is_logged_in", true)
                apply()
            }
            // ← AGREGAR ESTO: Migrar datos antiguos si existen
            val migrationCompleted = sharedPreferences.getBoolean("migration_completed", false)
            if (!migrationCompleted && sharedPreferences.contains("journal_entries")) {
                MigrationHelper(this@LoginActivity).migrateSharedPreferencesToRoom(userId)
            }
            Snackbar.make(rootView, "¡Cuenta creada exitosamente!", Snackbar.LENGTH_SHORT).show()
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}