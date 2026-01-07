package com.example.mentalhealthapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GamesMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_menu)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mini-Juegos Anti-Estrés"

        findViewById<CardView>(R.id.cardBubbleGame).setOnClickListener {
            startActivity(Intent(this, BubbleGameActivity::class.java))
        }

        findViewById<CardView>(R.id.cardMemoryGame).setOnClickListener {
            startActivity(Intent(this, MemoryGameActivity::class.java))
        }

        findViewById<CardView>(R.id.cardColorGame).setOnClickListener {
            startActivity(Intent(this, ColorGameActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}