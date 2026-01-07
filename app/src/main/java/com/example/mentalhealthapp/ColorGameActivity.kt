package com.example.mentalhealthapp

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import kotlinx.coroutines.launch

class ColorGameActivity : AppCompatActivity() {

    private lateinit var textQuestion: TextView
    private lateinit var textScore: TextView
    private lateinit var textTime: TextView
    private lateinit var buttonsContainer: LinearLayout
    private lateinit var rootView: android.view.View

    private var score = 0
    private var timeLeft = 30
    private var gameTimer: CountDownTimer? = null
    private var highScore = 0
    private var correctAnswers = 0
    private var totalQuestions = 0
    private var isPaused = false

    private val colorNames = listOf("ROJO", "AZUL", "VERDE", "AMARILLO", "NARANJA", "MORADO")
    private val colorValues = listOf(
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.parseColor("#FF9800"),
        Color.parseColor("#9C27B0")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Color Match"

        rootView = findViewById(android.R.id.content)
        textQuestion = findViewById(R.id.textQuestion)
        textScore = findViewById(R.id.textScore)
        textTime = findViewById(R.id.textTime)
        buttonsContainer = findViewById(R.id.buttonsContainer)

        // Cargar mejor puntuación
        val sharedPreferences = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(this@ColorGameActivity)
                val stats = database.userStatsDao().getStats(currentUserId)
                stats?.let {
                    highScore = it.colorHighScore
                    if (highScore > 0) {
                        Snackbar.make(rootView, "Récord a superar: $highScore puntos", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Restaurar estado o iniciar nuevo juego
        if (savedInstanceState != null) {
            score = savedInstanceState.getInt("score", 0)
            timeLeft = savedInstanceState.getInt("timeLeft", 30)
            correctAnswers = savedInstanceState.getInt("correctAnswers", 0)
            totalQuestions = savedInstanceState.getInt("totalQuestions", 0)
            updateScore()
            startTimer()
            nextRound()
        } else {
            startGame()
        }

        if (highScore > 0) {
            Snackbar.make(rootView, "Récord a superar: $highScore puntos", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("score", score)
        outState.putInt("timeLeft", timeLeft)
        outState.putInt("correctAnswers", correctAnswers)
        outState.putInt("totalQuestions", totalQuestions)
    }

    override fun onPause() {
        super.onPause()
        // Pausar el timer
        isPaused = true
        gameTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        // Reanudar el timer si el juego estaba activo
        if (isPaused && timeLeft > 0) {
            isPaused = false
            startTimer()
            Snackbar.make(rootView, "Juego reanudado", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        // Guardar estadísticas
        val prefs = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val totalGames = prefs.getInt("color_total_games", 0)
        val totalCorrect = prefs.getInt("color_total_correct", 0)

        prefs.edit().apply {
            putInt("color_total_games", totalGames + 1)
            putInt("color_total_correct", totalCorrect + correctAnswers)
            putInt("color_last_score", score)
            apply()
        }
    }

    private fun startGame() {
        score = 0
        timeLeft = 30
        correctAnswers = 0
        totalQuestions = 0
        updateScore()
        startTimer()
        nextRound()
    }

    private fun startTimer() {
        gameTimer?.cancel()
        gameTimer = object : CountDownTimer((timeLeft * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
                textTime.text = "⏱️ Tiempo: ${timeLeft}s"

                // Advertencia cuando quedan 5 segundos
                if (timeLeft == 5) {
                    Snackbar.make(rootView, "⚠️ ¡Solo 5 segundos!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#FF5722"))
                        .show()
                }
            }

            override fun onFinish() {
                endGame()
            }
        }.start()
    }

    private fun nextRound() {
        if (isPaused || timeLeft <= 0) return

        buttonsContainer.removeAllViews()
        totalQuestions++

        val correctIndex = Random.nextInt(colorNames.size)
        val correctColor = colorNames[correctIndex]

        val textColorIndex = Random.nextInt(colorNames.size)
        val textColor = colorValues[textColorIndex]

        textQuestion.text = correctColor
        textQuestion.setTextColor(textColor)

        val options = mutableListOf(correctIndex)
        while (options.size < 4) {
            val randomIndex = Random.nextInt(colorNames.size)
            if (randomIndex !in options) {
                options.add(randomIndex)
            }
        }
        options.shuffle()

        for (optionIndex in options) {
            val button = Button(this).apply {
                text = colorNames[optionIndex]
                textSize = 18f
                setBackgroundColor(colorValues[optionIndex])
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setPadding(32, 32, 32, 32)

                setOnClickListener {
                    checkAnswer(optionIndex, correctIndex)
                }
            }
            buttonsContainer.addView(button)
        }
    }

    private fun checkAnswer(selected: Int, correct: Int) {
        if (selected == correct) {
            score += 10
            correctAnswers++
            updateScore()

            // Bonus por racha
            if (correctAnswers % 5 == 0) {
                score += 25
                Snackbar.make(rootView, "¡RACHA! +25 puntos bonus 🔥", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FF9800"))
                    .show()
            }

            nextRound()
        } else {
            score -= 5
            if (score < 0) score = 0
            updateScore()

            Snackbar.make(rootView, "Incorrecto -5", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.parseColor("#F44336"))
                .show()
        }
    }

    private fun updateScore() {
        textScore.text = "🏆 Puntos: $score"
    }

    private fun endGame() {
        gameTimer?.cancel()
        isPaused = true

        val accuracy = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0

        val sharedPreferences = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(this@ColorGameActivity)

                // Actualizar estadísticas
                database.userStatsDao().incrementColorGamesPlayed(currentUserId)
                database.userStatsDao().updateColorHighScore(currentUserId, score)

                // Obtener nuevo récord
                val stats = database.userStatsDao().getStats(currentUserId)
                stats?.let {
                    highScore = it.colorHighScore

                    if (score == highScore) {
                        Snackbar.make(
                            rootView,
                            "🏆 ¡NUEVO RÉCORD! $score puntos\nPrecisión: $accuracy%",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("OK") {
                            finish()
                        }.show()
                    } else {
                        Snackbar.make(
                            rootView,
                            "Juego terminado: $score puntos\nPrecisión: $accuracy%\nRécord: $highScore",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Salir") {
                            finish()
                        }.show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameTimer?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}