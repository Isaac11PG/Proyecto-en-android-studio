package com.example.mentalhealthapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MeditationActivity : AppCompatActivity() {

    private lateinit var rootView: View
    private lateinit var containerMeditations: LinearLayout
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Meditación Guiada"

        rootView = findViewById(android.R.id.content)
        containerMeditations = findViewById(R.id.containerMeditations)

        val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("current_user_id", -1)

        setupMeditationCards()
    }

    private fun setupMeditationCards() {
        // Meditación para principiantes
        addMeditationCard(
            "🌱 Meditación para Principiantes",
            "Perfecta si es tu primera vez meditando",
            "5 minutos",
            "#E1BEE7",
            5
        )

        // Meditación para dormir
        addMeditationCard(
            "🌙 Meditación para Dormir",
            "Relájate profundamente antes de dormir",
            "10 minutos",
            "#B2EBF2",
            10
        )

        // Meditación anti-ansiedad
        addMeditationCard(
            "🌊 Calma la Ansiedad",
            "Reduce la ansiedad y encuentra paz interior",
            "7 minutos",
            "#C5E1A5",
            7
        )

        // Meditación de gratitud
        addMeditationCard(
            "💚 Gratitud Diaria",
            "Cultiva una actitud de agradecimiento",
            "5 minutos",
            "#FFE082",
            5
        )

        // Meditación body scan
        addMeditationCard(
            "🧘‍♀️ Escaneo Corporal",
            "Conecta con tu cuerpo y libera tensiones",
            "12 minutos",
            "#FFCCBC",
            12
        )

        // Meditación de enfoque
        addMeditationCard(
            "🎯 Mejora tu Enfoque",
            "Aumenta tu concentración y claridad mental",
            "8 minutos",
            "#D1C4E9",
            8
        )
    }

    private fun addMeditationCard(
        title: String,
        description: String,
        duration: String,
        colorHex: String,
        durationMinutes: Int
    ) {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 16f
            cardElevation = 8f
            setCardBackgroundColor(android.graphics.Color.parseColor(colorHex))
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(resources.getColor(R.color.text_primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val descriptionText = TextView(this).apply {
            text = description
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(0, 8, 0, 16)
        }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val durationText = TextView(this).apply {
            text = "⏱️ $duration"
            textSize = 14f
            setTextColor(resources.getColor(R.color.primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val startButton = Button(this).apply {
            text = "▶️ Comenzar"
            setBackgroundColor(resources.getColor(R.color.primary, null))
            setTextColor(resources.getColor(R.color.white, null))

            setOnClickListener {
                startMeditation(title, durationMinutes)
            }
        }

        infoLayout.addView(durationText)
        infoLayout.addView(startButton)

        layout.addView(titleText)
        layout.addView(descriptionText)
        layout.addView(infoLayout)

        card.addView(layout)
        containerMeditations.addView(card)
    }

    private fun startMeditation(title: String, durationMinutes: Int) {
        val intent = android.content.Intent(this, MeditationSessionActivity::class.java).apply {
            putExtra("meditation_title", title)
            putExtra("meditation_duration", durationMinutes)
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}