package com.example.ballgame

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null

    val player = Player()
    val camera = Camera()
    val level = Level()
    val ui = UI()
    val background = TempleBackground()

    var leftPressed = false
    var rightPressed = false
    var jumpPressed = false
    var tapX = 0f
    var tapY = 0f
    var tapHandled = false

    var screenW = 0
    var screenH = 0

    private val ZOOM = 1.8f

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenW = width
        screenH = height
        background.setScreen(screenW, screenH)
        ui.updateButtons(screenW, screenH)
        ui.buildLevelButtons(screenW, screenH, SaveManager.getHighestUnlocked())

        gameThread = GameThread(holder, this)
        gameThread?.setRunning(true)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        screenW = w
        screenH = h
        background.setScreen(w, h)
        ui.updateButtons(w, h)
        ui.buildLevelButtons(w, h, SaveManager.getHighestUnlocked())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopThread()
    }

    private fun stopThread() {
        gameThread?.setRunning(false)
        var retry = true
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {}
        }
        gameThread = null
    }

    fun pauseGame() { stopThread() }

    fun resumeGame() {
        if (gameThread == null) {
            gameThread = GameThread(holder, this)
            gameThread?.setRunning(true)
            gameThread?.start()
        }
    }

    private fun startRespawn() {
        GameState.isRespawning = true
        GameState.respawnTimer = 1.5f  // 1.5 second delay
    }

    private fun doRespawn() {
        player.reset()
        camera.x = 0f
        camera.y = 0f
        GameState.isRespawning = false
        GameState.respawnTimer = 0f
    }

    fun update(dt: Float) {
        if (tapHandled) {
            when (GameState.screen) {
                GameState.SCREEN_MENU -> handleMenuTap(tapX, tapY)
                GameState.SCREEN_LEVEL_SELECT -> handleLevelSelectTap(tapX, tapY)
                GameState.SCREEN_PLAYING -> handlePlayingTap(tapX, tapY)
                GameState.SCREEN_PAUSED -> handlePausedTap(tapX, tapY)
            }
            tapHandled = false
        }

        if (GameState.screen != GameState.SCREEN_PLAYING) return
        if (GameState.gameOver) return

        // ---- RESPAWN COUNTDOWN ----
        if (GameState.isRespawning) {
            GameState.respawnTimer -= dt
            if (GameState.respawnTimer <= 0f) {
                doRespawn()
            }
            // Still update UI buttons while waiting
            ui.updateButtons(screenW, screenH)
            return
        }

        if (GameState.levelComplete) {
            GameState.levelCompleteTimer -= dt
            if (GameState.levelCompleteTimer <= 0f) {
                SaveManager.setBestScore(GameState.currentLevel, GameState.score)
                SaveManager.setHighestUnlocked(GameState.currentLevel + 1)

                if (GameState.currentLevel < 20) {
                    GameState.resetLevel(GameState.currentLevel + 1)
                    level.loadLevel(GameState.currentLevel)
                    player.reset()
                    camera.x = 0f
                    camera.y = 0f
                    GameState.levelComplete = false
                } else {
                    GameState.won = true
                    GameState.gameOver = true
                    GameState.levelComplete = false
                }
            }
            return
        }

        player.update(dt, leftPressed, rightPressed, jumpPressed, level, screenW, screenH)
        jumpPressed = false

        camera.followPlayer(player, level.width, screenW, screenH, zoom = ZOOM)
        level.updateEnemies(dt)

        // ---- SPIKE HIT ----
        if (level.checkSpikeCollision(player)) {
            player.hit()
            if (GameState.lives <= 0) {
                GameState.gameOver = true
                SaveManager.setBestScore(GameState.currentLevel, GameState.score)
            } else {
                startRespawn()
            }
            ui.updateButtons(screenW, screenH)
            return
        }

        // ---- ENEMY HIT ----
        level.checkEnemyCollision(player) { pts -> GameState.score += pts }
        if (GameState.lives <= 0) {
            GameState.gameOver = true
            SaveManager.setBestScore(GameState.currentLevel, GameState.score)
            return
        }
        // Check if player died from enemy collision (lives changed but still > 0)
        if (player.invulnerable > 1.4f && GameState.lives > 0 && !GameState.isRespawning) {
            // A fresh hit was taken from an enemy — start respawn
            startRespawn()
            ui.updateButtons(screenW, screenH)
            return
        }

        // ---- PORTAL REACHED ----
        if (level.checkPortalCollision(player) && !GameState.levelComplete) {
            GameState.score += 100
            GameState.levelComplete = true
            GameState.levelCompleteTimer = 2.0f
        }

        // ---- FELL OFF SCREEN ----
        if (player.y > screenH + 400) {
            player.hit()
            if (GameState.lives <= 0) {
                GameState.gameOver = true
                SaveManager.setBestScore(GameState.currentLevel, GameState.score)
            } else {
                startRespawn()
            }
            ui.updateButtons(screenW, screenH)
            return
        }

        ui.updateButtons(screenW, screenH)
    }

    fun render(canvas: Canvas) {
        when (GameState.screen) {
            GameState.SCREEN_MENU -> {
                background.draw(canvas, 0f)
                ui.drawMenu(canvas, screenW, screenH)
            }
            GameState.SCREEN_LEVEL_SELECT -> {
                background.draw(canvas, 0f)
                ui.drawLevelSelect(canvas, screenW, screenH)
            }
            GameState.SCREEN_PLAYING, GameState.SCREEN_PAUSED -> {
                background.draw(canvas, camera.x)

                canvas.save()
                canvas.scale(ZOOM, ZOOM)
                canvas.translate(-camera.x, -camera.y)
                level.draw(canvas, camera, (screenW / ZOOM).toInt())

                // Only draw player if NOT respawning
                if (!GameState.isRespawning) {
                    player.draw(canvas)
                }
                canvas.restore()

                if (GameState.screen == GameState.SCREEN_PLAYING) {
                    ui.drawGameUI(canvas, player, screenW, screenH)
                    // Draw respawn countdown overlay
                    if (GameState.isRespawning) {
                        ui.drawRespawnOverlay(canvas, screenW, screenH, GameState.respawnTimer)
                    }
                } else {
                    ui.drawPauseMenu(canvas, screenW, screenH)
                }
            }
        }
    }

    private fun handleMenuTap(x: Float, y: Float) {
        if (ui.playButton.contains(x, y)) {
            GameState.screen = GameState.SCREEN_LEVEL_SELECT
            ui.buildLevelButtons(screenW, screenH, SaveManager.getHighestUnlocked())
        }
    }

    private fun handleLevelSelectTap(x: Float, y: Float) {
        if (ui.backButton.contains(x, y)) {
            GameState.screen = GameState.SCREEN_MENU
            return
        }
        val highest = SaveManager.getHighestUnlocked()
        for (i in ui.levelButtons.indices) {
            if (ui.levelButtons[i].contains(x, y)) {
                val levelNum = i + 1
                if (levelNum <= highest) {
                    GameState.resetLevel(levelNum)
                    level.loadLevel(levelNum)
                    player.reset()
                    camera.x = 0f
                    camera.y = 0f
                    GameState.screen = GameState.SCREEN_PLAYING
                }
                return
            }
        }
    }

    private fun handlePlayingTap(x: Float, y: Float) {
        if (ui.pauseButton.contains(x, y)) {
            GameState.screen = GameState.SCREEN_PAUSED
            return
        }

        if (GameState.isRespawning) return  // ignore taps during respawn

        if (GameState.gameOver) {
            if (ui.resetButton.contains(x, y)) {
                GameState.resetLevel(GameState.currentLevel)
                level.loadLevel(GameState.currentLevel)
                player.reset()
                camera.x = 0f
                camera.y = 0f
            }
            return
        }

        if (ui.resetButton.contains(x, y)) {
            GameState.resetLevel(GameState.currentLevel)
            level.loadLevel(GameState.currentLevel)
            player.reset()
            camera.x = 0f
            camera.y = 0f
            return
        }
        if (ui.leftButton.contains(x, y)) { leftPressed = true; return }
        if (ui.rightButton.contains(x, y)) { rightPressed = true; return }
        if (ui.jumpButton.contains(x, y)) { jumpPressed = true; return }
    }

    private fun handlePausedTap(x: Float, y: Float) {
        if (ui.resumeButton.contains(x, y)) {
            GameState.screen = GameState.SCREEN_PLAYING
        }
        if (ui.menuButton.contains(x, y)) {
            GameState.screen = GameState.SCREEN_LEVEL_SELECT
            ui.buildLevelButtons(screenW, screenH, SaveManager.getHighestUnlocked())
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.getX(event.actionIndex)
                val y = event.getY(event.actionIndex)
                tapX = x; tapY = y; tapHandled = true

                if (GameState.screen == GameState.SCREEN_PLAYING &&
                    !GameState.gameOver && !GameState.isRespawning) {
                    if (ui.leftButton.contains(x, y)) leftPressed = true
                    if (ui.rightButton.contains(x, y)) rightPressed = true
                    if (ui.jumpButton.contains(x, y)) jumpPressed = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val x = event.getX(event.actionIndex)
                val y = event.getY(event.actionIndex)
                if (ui.leftButton.contains(x, y)) leftPressed = false
                if (ui.rightButton.contains(x, y)) rightPressed = false
                if (ui.jumpButton.contains(x, y)) jumpPressed = false
            }
            MotionEvent.ACTION_CANCEL -> {
                leftPressed = false; rightPressed = false; jumpPressed = false
            }
        }
        return true
    }
}