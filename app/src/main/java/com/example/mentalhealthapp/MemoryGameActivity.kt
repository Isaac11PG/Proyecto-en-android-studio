package com.example.mentalhealthapp

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import kotlinx.coroutines.launch

class MemoryGameActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var movesText: TextView
    private lateinit var resetButton: Button
    private lateinit var rootView: android.view.View

    private val emojis = listOf("😊", "🌟", "🎨", "🎵", "🌈", "🦋", "🌸", "⭐")
    private var cards = mutableListOf<String>()
    private var cardButtons = mutableListOf<Button>()
    private var flippedIndices = mutableListOf<Int>()
    private var firstCard: Button? = null
    private var secondCard: Button? = null
    private var isProcessing = false
    private var moves = 0
    private var matchesFound = 0
    private var gameStartTime = 0L
    private var bestScore = Int.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Juego de Memoria"

        rootView = findViewById(android.R.id.content)
        gridLayout = findViewById(R.id.gridLayout)
        movesText = findViewById(R.id.movesText)
        resetButton = findViewById(R.id.resetButton)

        // Cargar mejor puntuación de la base de datos
        val sharedPreferences = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        // ... dentro de onCreate ...
        if (currentUserId != -1) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(this@MemoryGameActivity)
                val stats = database.userStatsDao().getStats(currentUserId)
                stats?.let {
                    // Suponiendo que 999 o Int.MAX_VALUE es el inicial
                    bestScore = it.memoryBestScore

                    // Validamos: Si el record es mayor a 500 (o el máximo), no lo mostramos como record real
                    if (bestScore > 0 && bestScore < 999) {
                        Snackbar.make(rootView, "Mejor puntuación: $bestScore movimientos", Snackbar.LENGTH_SHORT).show()
                    } else {
                        // Si es 999 o MAX_VALUE, reiniciamos bestScore internamente para que
                        // cualquier partida nueva sea el nuevo record
                        bestScore = Int.MAX_VALUE
                    }
                }
            }
        }

        // Recibir nivel de dificultad si viene de otra activity
        val difficulty = intent.getStringExtra("difficulty") ?: "normal"

        resetButton.setOnClickListener {
            resetGame()
        }

        // Restaurar estado o iniciar nuevo juego
        if (savedInstanceState != null) {
            restoreGameState(savedInstanceState)
        } else {
            gameStartTime = System.currentTimeMillis()
            initializeGame()
        }

        if (bestScore != Int.MAX_VALUE) {
            Snackbar.make(rootView, "Mejor puntuación: $bestScore movimientos", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("cards", ArrayList(cards))
        outState.putIntegerArrayList("flippedIndices", ArrayList(flippedIndices))
        outState.putInt("moves", moves)
        outState.putInt("matchesFound", matchesFound)
        outState.putLong("gameStartTime", gameStartTime)
    }

    private fun restoreGameState(savedInstanceState: Bundle) {
        cards = savedInstanceState.getStringArrayList("cards")?.toMutableList() ?: mutableListOf()
        flippedIndices = savedInstanceState.getIntegerArrayList("flippedIndices")?.toMutableList() ?: mutableListOf()
        moves = savedInstanceState.getInt("moves", 0)
        matchesFound = savedInstanceState.getInt("matchesFound", 0)
        gameStartTime = savedInstanceState.getLong("gameStartTime", System.currentTimeMillis())

        rebuildGameBoard()
        updateMoves()

        Snackbar.make(rootView, "Juego restaurado", Snackbar.LENGTH_SHORT).show()
    }

    private fun rebuildGameBoard() {
        gridLayout.removeAllViews()
        cardButtons.clear()

        for (i in cards.indices) {
            val button = Button(this).apply {
                text = if (flippedIndices.contains(i)) cards[i] else "?"
                textSize = 32f
                setBackgroundColor(
                    if (flippedIndices.contains(i)) Color.parseColor("#00E676")
                    else Color.parseColor("#7C4DFF")
                )
                setTextColor(Color.WHITE)
                isEnabled = !flippedIndices.contains(i)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 200
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    onCardClick(this, i)
                }
            }

            cardButtons.add(button)
            gridLayout.addView(button)
        }
    }

    override fun onPause() {
        super.onPause()
        // Guardar progreso actual
        val prefs = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("memory_current_moves", moves)
            putInt("memory_current_matches", matchesFound)
            apply()
        }
    }

    override fun onStop() {
        super.onStop()
        // Guardar estadísticas de juego
        val prefs = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val totalGames = prefs.getInt("memory_total_games", 0)
        prefs.edit().apply {
            putInt("memory_total_games", totalGames + 1)
            putInt("memory_last_moves", moves)
            apply()
        }
    }

    private fun initializeGame() {
        cards.clear()
        cards.addAll(emojis)
        cards.addAll(emojis)
        cards.shuffle()

        flippedIndices.clear()

        gridLayout.removeAllViews()
        cardButtons.clear()

        for (i in cards.indices) {
            val button = Button(this).apply {
                text = "?"
                textSize = 32f
                setBackgroundColor(Color.parseColor("#7C4DFF"))
                setTextColor(Color.WHITE)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 200
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    onCardClick(this, i)
                }
            }

            cardButtons.add(button)
            gridLayout.addView(button)
        }

        moves = 0
        matchesFound = 0
        updateMoves()
    }

    private fun onCardClick(button: Button, index: Int) {
        if (isProcessing || button.text != "?" || button == firstCard) return

        button.text = cards[index]
        button.setBackgroundColor(Color.parseColor("#00E676"))

        if (firstCard == null) {
            firstCard = button
        } else if (secondCard == null) {
            secondCard = button
            isProcessing = true
            moves++
            updateMoves()

            Handler(Looper.getMainLooper()).postDelayed({
                checkMatch()
            }, 1000)
        }
    }

    private fun checkMatch() {
        val firstIndex = cardButtons.indexOf(firstCard)
        val secondIndex = cardButtons.indexOf(secondCard)

        if (cards[firstIndex] == cards[secondIndex]) {
            firstCard?.isEnabled = false
            secondCard?.isEnabled = false
            flippedIndices.add(firstIndex)
            flippedIndices.add(secondIndex)
            matchesFound++

            Snackbar.make(rootView, "¡Pareja encontrada! 🎉", Snackbar.LENGTH_SHORT).show()

            if (matchesFound == emojis.size) {
                gameCompleted()
            }
        } else {
            firstCard?.text = "?"
            firstCard?.setBackgroundColor(Color.parseColor("#7C4DFF"))
            secondCard?.text = "?"
            secondCard?.setBackgroundColor(Color.parseColor("#7C4DFF"))
        }

        firstCard = null
        secondCard = null
        isProcessing = false
    }

    private fun gameCompleted() {
        val timePlayed = (System.currentTimeMillis() - gameStartTime) / 1000

        val sharedPreferences = getSharedPreferences("MindCarePrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(this@MemoryGameActivity)

                // Actualizar estadísticas
                database.userStatsDao().incrementMemoryGamesPlayed(currentUserId)
                database.userStatsDao().updateMemoryBestScore(currentUserId, moves)

                // Obtener nuevo récord
                val stats = database.userStatsDao().getStats(currentUserId)
                stats?.let {
                    bestScore = it.memoryBestScore

                    if (moves == bestScore) {
                        Snackbar.make(
                            rootView,
                            "¡Nuevo récord! 🏆 Completado en $moves movimientos",
                            Snackbar.LENGTH_LONG
                        ).setAction("Compartir") {
                            // Funcionalidad de compartir
                        }.show()
                    } else {
                        Snackbar.make(
                            rootView,
                            "¡Ganaste! 🎉 Movimientos: $moves | Tiempo: ${timePlayed}s",
                            Snackbar.LENGTH_LONG
                        ).setAction("Reintentar") {
                            resetGame()
                        }.show()
                    }
                }
            }
        }
    }

    private fun updateMoves() {
        movesText.text = "Movimientos: $moves"
    }

    private fun resetGame() {
        gameStartTime = System.currentTimeMillis()
        initializeGame()
        Snackbar.make(rootView, "Nuevo juego iniciado", Snackbar.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}