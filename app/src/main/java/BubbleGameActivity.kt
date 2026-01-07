package com.example.mentalhealthapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import kotlinx.coroutines.launch

class BubbleGameActivity : AppCompatActivity() {

    private lateinit var gameContainer: FrameLayout
    private lateinit var scoreText: TextView
    private lateinit var rootView: View
    private var score = 0
    private var isRunning = true
    private var bubblesPopped = 0
    private var gameStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Revienta Burbujas"

        rootView = findViewById(android.R.id.content)
        gameContainer = findViewById(R.id.gameContainer)
        scoreText = findViewById(R.id.scoreText)

        // Recibir datos si vienen de otra actividad
        score = intent.getIntExtra("previous_score", 0)

        // Restaurar estado
        savedInstanceState?.let {
            score = it.getInt("score", 0)
            bubblesPopped = it.getInt("bubblesPopped", 0)
            gameStartTime = it.getLong("gameStartTime", System.currentTimeMillis())
        } ?: run {
            gameStartTime = System.currentTimeMillis()
        }

        updateScore()

        Snackbar.make(rootView, "¡Toca las burbujas para relajarte!", Snackbar.LENGTH_SHORT).show()

        startGame()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("score", score)
        outState.putInt("bubblesPopped", bubblesPopped)
        outState.putLong("gameStartTime", gameStartTime)
    }

    override fun onPause() {
        super.onPause()
        // Detener creación de burbujas cuando no está visible
        isRunning = false
    }

    override fun onResume() {
        super.onResume()
        // Reanudar juego
        isRunning = true
        if (gameContainer.childCount == 0) {
            createBubbles()
        }
    }

    override fun onStop() {
        super.onStop()

        val sharedPreferences = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId != -1 && bubblesPopped > 0) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(this@BubbleGameActivity)

                // Actualizar estadísticas
                database.userStatsDao().incrementBubblesPopped(currentUserId, bubblesPopped)
                database.userStatsDao().updateBubbleHighScore(currentUserId, score)

                // Verificar logro
                val stats = database.userStatsDao().getStats(currentUserId)
                stats?.let {
                    if (it.totalBubblesPopped >= 100 && !it.achievementBubbleMaster) {
                        database.userStatsDao().unlockBubbleMaster(currentUserId)
                        NotificationHelper(this@BubbleGameActivity).sendAchievementNotification(
                            "Maestro de Burbujas",
                            "¡Has reventado 100 burbujas!"
                        )
                    }
                }
            }
        }
    }

    private fun startGame() {
        score = intent.getIntExtra("previous_score", score)
        updateScore()
        createBubbles()
    }

    private fun createBubbles() {
        if (!isRunning) return

        val bubble = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                Random.nextInt(100, 200),
                Random.nextInt(100, 200)
            )

            val colors = listOf(
                Color.parseColor("#E1BEE7"),
                Color.parseColor("#B2EBF2"),
                Color.parseColor("#FFE082"),
                Color.parseColor("#FFCCBC"),
                Color.parseColor("#C5E1A5")
            )
            setBackgroundColor(colors.random())
            alpha = 0.8f

            val screenWidth = resources.displayMetrics.widthPixels
            x = Random.nextInt(0, screenWidth - 200).toFloat()
            y = resources.displayMetrics.heightPixels.toFloat()

            background = resources.getDrawable(R.drawable.circle_bubble, null)
        }

        bubble.setOnClickListener {
            popBubble(it)
        }

        gameContainer.addView(bubble)
        animateBubble(bubble)

        gameContainer.postDelayed({
            createBubbles()
        }, Random.nextLong(500, 1500))
    }

    private fun animateBubble(bubble: View) {
        val animator = ObjectAnimator.ofFloat(
            bubble,
            "translationY",
            bubble.y,
            -300f
        ).apply {
            duration = Random.nextLong(3000, 6000)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    gameContainer.removeView(bubble)
                }
            })
            start()
        }
    }

    private fun popBubble(bubble: View) {
        score += 10
        bubblesPopped++
        updateScore()

        // Mostrar feedback cada 5 burbujas
        if (bubblesPopped % 5 == 0) {
            Snackbar.make(rootView, "¡${bubblesPopped} burbujas! 🎉", Snackbar.LENGTH_SHORT).show()
        }

        bubble.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                gameContainer.removeView(bubble)
            }
            .start()
    }

    private fun updateScore() {
        scoreText.text = "Puntos: $score"
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false

        // Mostrar resumen al salir
        val timePlayed = (System.currentTimeMillis() - gameStartTime) / 1000
        val prefs = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putLong("bubble_time_played", timePlayed)
            apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Pasar datos de vuelta
        val timePlayed = (System.currentTimeMillis() - gameStartTime) / 1000
        Snackbar.make(
            rootView,
            "Jugaste ${timePlayed}s y reventaste $bubblesPopped burbujas",
            Snackbar.LENGTH_LONG
        ).show()

        finish()
        return true
    }
}