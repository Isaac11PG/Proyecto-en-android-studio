package com.example.mentalhealthapp

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import kotlinx.coroutines.launch

class BreathingActivity : AppCompatActivity() {

    private lateinit var circleView: View
    private lateinit var textInstruction: TextView
    private lateinit var textCounter: TextView
    private lateinit var buttonStart: Button
    private lateinit var rootView: View

    private var isRunning = false
    private var currentTimer: CountDownTimer? = null
    private var currentAnimator: ValueAnimator? = null
    private var currentPhase = 0 // 0=inhale, 1=hold, 2=exhale
    private var cyclesCompleted = 0
    private var timeLeftInPhase = 0L
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ejercicios de Respiración"

        rootView = findViewById(android.R.id.content)
        circleView = findViewById(R.id.circleView)
        textInstruction = findViewById(R.id.textInstruction)
        textCounter = findViewById(R.id.textCounter)
        buttonStart = findViewById(R.id.buttonStart)

        // Restaurar estado si existe
        savedInstanceState?.let {
            isRunning = it.getBoolean("isRunning", false)
            currentPhase = it.getInt("currentPhase", 0)
            cyclesCompleted = it.getInt("cyclesCompleted", 0)
            timeLeftInPhase = it.getLong("timeLeftInPhase", 0)

            if (isRunning) {
                buttonStart.text = "⏸️ Detener"
                Snackbar.make(rootView, "Ejercicio restaurado", Snackbar.LENGTH_SHORT).show()
            }
        }

        buttonStart.setOnClickListener {
            if (isRunning) {
                stopExercise()
            } else {
                startExercise()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isRunning", isRunning)
        outState.putInt("currentPhase", currentPhase)
        outState.putInt("cyclesCompleted", cyclesCompleted)
        outState.putLong("timeLeftInPhase", timeLeftInPhase)
    }

    override fun onPause() {
        super.onPause()
        // Pausar animaciones y timers si está corriendo
        if (isRunning) {
            isPaused = true
            currentTimer?.cancel()
            currentAnimator?.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reanudar ejercicio si estaba corriendo
        if (isRunning && isPaused) {
            isPaused = false
            Snackbar.make(rootView, "Continuando ejercicio...", Snackbar.LENGTH_SHORT).show()
            continueExercise()
        }
    }

    override fun onStop() {
        super.onStop()
        // Guardar progreso cuando la actividad no es visible
        if (isRunning) {
            val prefs = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
            prefs.edit().apply {
                putInt("breathing_cycles", cyclesCompleted)
                putLong("breathing_last_session", System.currentTimeMillis())
                apply()
            }
        }
    }

    private fun startExercise() {
        isRunning = true
        cyclesCompleted = 0
        buttonStart.text = "⏸️ Detener"

        Snackbar.make(rootView, "Ejercicio iniciado. Relájate y respira", Snackbar.LENGTH_SHORT)
            .setAction("OK") { }
            .show()

        breatheInCycle()
    }

    private fun continueExercise() {
        when (currentPhase) {
            0 -> breatheInCycle()
            1 -> hold { breatheOut { breatheInCycle() } }
            2 -> breatheOut { breatheInCycle() }
        }
    }

    private fun stopExercise() {
        isRunning = false
        isPaused = false
        buttonStart.text = "▶️ Comenzar"
        currentTimer?.cancel()
        currentAnimator?.cancel()

        textInstruction.text = "Presiona comenzar"
        textCounter.text = ""
        circleView.scaleX = 1f
        circleView.scaleY = 1f

        if (cyclesCompleted > 0) {
            // Guardar estadísticas en la base de datos
            val sharedPreferences = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
            val currentUserId = sharedPreferences.getInt("current_user_id", -1)

            if (currentUserId != -1) {
                lifecycleScope.launch {
                    val database = AppDatabase.getDatabase(this@BreathingActivity)
                    database.userStatsDao().updateBreathingStats(currentUserId, cyclesCompleted)

                    // Verificar logro
                    val stats = database.userStatsDao().getStats(currentUserId)
                    stats?.let {
                        if (it.totalBreathingSessions >= 5 && !it.achievementBreathingZen) {
                            database.userStatsDao().unlockBreathingZen(currentUserId)
                            NotificationHelper(this@BreathingActivity).sendAchievementNotification(
                                "Respirador Zen",
                                "¡Has completado 5 sesiones de respiración!"
                            )
                        }
                    }
                }
            }

            Snackbar.make(
                rootView,
                "Completaste $cyclesCompleted ciclos. ¡Bien hecho! 🌟",
                Snackbar.LENGTH_LONG
            ).setAction("Compartir") {
                // Funcionalidad de compartir
            }.show()
        }
    }

    private fun breatheInCycle() {
        currentPhase = 0
        breatheIn {
            currentPhase = 1
            hold {
                currentPhase = 2
                breatheOut {
                    cyclesCompleted++
                    if (isRunning && !isPaused) {
                        breatheInCycle()
                    }
                }
            }
        }
    }

    private fun breatheIn(onComplete: () -> Unit) {
        textInstruction.text = "🌬️ Inhala profundamente"
        textInstruction.setTextColor(ContextCompat.getColor(this, R.color.accent))

        animateCircle(1f, 2f, 4000)
        countdown(4, onComplete)
    }

    private fun hold(onComplete: () -> Unit) {
        textInstruction.text = "⏸️ Sostén la respiración"
        textInstruction.setTextColor(ContextCompat.getColor(this, R.color.primary))

        countdown(4, onComplete)
    }

    private fun breatheOut(onComplete: () -> Unit) {
        textInstruction.text = "💨 Exhala lentamente"
        textInstruction.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))

        animateCircle(2f, 1f, 4000)
        countdown(4, onComplete)
    }

    private fun animateCircle(fromScale: Float, toScale: Float, duration: Long) {
        currentAnimator?.cancel()
        currentAnimator = ValueAnimator.ofFloat(fromScale, toScale).apply {
            this.duration = duration
            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                circleView.scaleX = scale
                circleView.scaleY = scale
            }
            start()
        }
    }

    private fun countdown(seconds: Int, onComplete: () -> Unit) {
        currentTimer?.cancel()
        currentTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInPhase = millisUntilFinished
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                textCounter.text = secondsLeft.toString()
            }

            override fun onFinish() {
                if (!isPaused) {
                    onComplete()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopExercise()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}