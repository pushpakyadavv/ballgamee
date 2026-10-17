package com.example.ballgame

object GameState {
    const val SCREEN_MENU = 0
    const val SCREEN_LEVEL_SELECT = 1
    const val SCREEN_PLAYING = 2
    const val SCREEN_PAUSED = 3

    var screen = SCREEN_MENU
    var score = 0
    var lives = 3
    var currentLevel = 1
    var gameOver = false
    var won = false
    var levelComplete = false
    var levelCompleteTimer = 0f

    // Respawn delay (in seconds)
    var respawnTimer = 0f
    var isRespawning = false

    fun resetLevel(level: Int) {
        score = 0
        lives = 3
        currentLevel = level
        gameOver = false
        won = false
        levelComplete = false
        levelCompleteTimer = 0f
        respawnTimer = 0f
        isRespawning = false
    }

    fun resetAll() {
        score = 0
        lives = 3
        currentLevel = 1
        gameOver = false
        won = false
        levelComplete = false
        levelCompleteTimer = 0f
        respawnTimer = 0f
        isRespawning = false
        screen = SCREEN_MENU
    }
}