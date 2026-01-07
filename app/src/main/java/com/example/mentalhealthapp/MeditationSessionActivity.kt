package com.example.mentalhealthapp

import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MeditationSessionActivity : AppCompatActivity() {

    private lateinit var rootView: View
    private lateinit var titleText: TextView
    private lateinit var timerText: TextView
    private lateinit var instructionText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var circleView: View
    private lateinit var buttonPlay: Button
    private lateinit var buttonStop: Button

    private var timer: CountDownTimer? = null
    private var animator: ValueAnimator? = null

    private var meditationTitle: String = ""
    private var totalDurationMinutes: Int = 5
    private var remainingTimeMillis: Long = 0
    private var isPaused = false
    private var isRunning = false

    private val phases = listOf(
        "Cierra los ojos suavemente...",
        "Respira profundamente...",
        "Relaja tus hombros...",
        "Observa tus pensamientos sin juzgar...",
        "Siente la calma en tu cuerpo...",
        "Conecta con el momento presente...",
        "Suelta cualquier tensión...",
        "Respira con tranquilidad...",
        "Permítete simplemente ser..."
    )

    private var currentPhaseIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation_session)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rootView = findViewById(android.R.id.content)
        titleText = findViewById(R.id.titleText)
        timerText = findViewById(R.id.timerText)
        instructionText = findViewById(R.id.instructionText)
        progressBar = findViewById(R.id.progressBar)
        circleView = findViewById(R.id.circleView)
        buttonPlay = findViewById(R.id.buttonPlay)
        buttonStop = findViewById(R.id.buttonStop)

        meditationTitle = intent.getStringExtra("meditation_title") ?: "Meditación"
        totalDurationMinutes = intent.getIntExtra("meditation_duration", 5)

        supportActionBar?.title = meditationTitle
        titleText.text = meditationTitle

        remainingTimeMillis = totalDurationMinutes * 60 * 1000L
        updateTimerDisplay()

        progressBar.max = totalDurationMinutes * 60
        progressBar.progress = totalDurationMinutes * 60

        buttonPlay.setOnClickListener {
            if (isRunning) {
                pauseMeditation()
            } else {
                startMeditation()
            }
        }

        buttonStop.setOnClickListener {
            showStopConfirmation()
        }

        startBreathingAnimation()
    }

    private fun startMeditation() {
        isRunning = true
        isPaused = false
        buttonPlay.text = "⏸️ Pausar"

        timer?.cancel()
        timer = object : CountDownTimer(remainingTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
                updateTimerDisplay()

                val secondsElapsed = totalDurationMinutes * 60 - (millisUntilFinished / 1000).toInt()
                progressBar.progress = (millisUntilFinished / 1000).toInt()

                // Cambiar instrucciones cada minuto
                if (secondsElapsed % 60 == 0 && secondsElapsed > 0) {
                    updateInstruction()
                }
            }

            override fun onFinish() {
                completeMeditation()
            }
        }.start()

        Snackbar.make(rootView, "Meditación iniciada. Relájate...", Snackbar.LENGTH_SHORT).show()
    }

    private fun pauseMeditation() {
        isRunning = false
        isPaused = true
        buttonPlay.text = "▶️ Continuar"
        timer?.cancel()

        Snackbar.make(rootView, "Meditación en pausa", Snackbar.LENGTH_SHORT).show()
    }

    private fun stopMeditation() {
        isRunning = false
        isPaused = false
        timer?.cancel()
        animator?.cancel()

        // Guardar progreso
        val timeCompleted = totalDurationMinutes * 60 - (remainingTimeMillis / 1000).toInt()
        saveMeditationProgress(timeCompleted)

        finish()
    }

    private fun completeMeditation() {
        isRunning = false
        timer?.cancel()

        // Guardar sesión completa
        saveMeditationProgress(totalDurationMinutes * 60)

        AlertDialog.Builder(this)
            .setTitle("¡Meditación Completada! 🎉")
            .setMessage("Has completado $totalDurationMinutes minutos de meditación.\n\n¿Cómo te sientes?")
            .setPositiveButton("Bien") { _, _ ->
                recordMood("bien")
                finish()
            }
            .setNeutralButton("Muy bien") { _, _ ->
                recordMood("muy_bien")
                finish()
            }
            .setNegativeButton("Relajado") { _, _ ->
                recordMood("relajado")
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showStopConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Detener meditación")
            .setMessage("¿Deseas detener la meditación ahora?")
            .setPositiveButton("Sí, detener") { _, _ ->
                stopMeditation()
            }
            .setNegativeButton("Continuar", null)
            .show()
    }

    private fun updateTimerDisplay() {
        val minutes = (remainingTimeMillis / 1000) / 60
        val seconds = (remainingTimeMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateInstruction() {
        if (currentPhaseIndex < phases.size) {
            instructionText.text = phases[currentPhaseIndex]
            currentPhaseIndex++

            // Animación de fade in
            instructionText.alpha = 0f
            instructionText.animate()
                .alpha(1f)
                .setDuration(1000)
                .start()
        }
    }

    private fun startBreathingAnimation() {
        animator = ValueAnimator.ofFloat(1f, 1.3f, 1f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                circleView.scaleX = scale
                circleView.scaleY = scale
            }

            start()
        }
    }

    private fun saveMeditationProgress(secondsCompleted: Int) {
        val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            val totalMeditationTime = sharedPreferences.getInt("total_meditation_time", 0)
            val totalSessions = sharedPreferences.getInt("total_meditation_sessions", 0)

            sharedPreferences.edit().apply {
                putInt("total_meditation_time", totalMeditationTime + secondsCompleted)
                putInt("total_meditation_sessions", totalSessions + 1)
                putLong("last_meditation_time", System.currentTimeMillis())
                apply()
            }

            // Verificar logro
            if (totalSessions + 1 == 1) {
                NotificationHelper(this).sendAchievementNotification(
                    "Primera Meditación",
                    "¡Has completado tu primera sesión de meditación!"
                )
            } else if (totalSessions + 1 == 10) {
                NotificationHelper(this).sendAchievementNotification(
                    "Meditador Dedicado",
                    "¡Has completado 10 sesiones de meditación!"
                )
            }
        }
    }

    private fun recordMood(mood: String) {
        val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("last_mood_after_meditation", mood).apply()

        Snackbar.make(rootView, "Estado registrado. ¡Gracias por meditar! 🙏", Snackbar.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if (isRunning) {
            pauseMeditation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        animator?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (isRunning) {
            showStopConfirmation()
        } else {
            finish()
        }
        return true
    }

    override fun onBackPressed() {
        if (isRunning) {
            showStopConfirmation()
        } else {
            super.onBackPressed()
        }
    }
}