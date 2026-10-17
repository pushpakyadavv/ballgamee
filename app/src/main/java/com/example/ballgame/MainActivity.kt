package com.example.ballgame

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var gameView: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock to landscape (horizontal) mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Fullscreen
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportActionBar?.hide()

        SaveManager.init(this)

        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onPause() { super.onPause(); gameView?.pauseGame() }
    override fun onResume() { super.onResume(); gameView?.resumeGame() }
}