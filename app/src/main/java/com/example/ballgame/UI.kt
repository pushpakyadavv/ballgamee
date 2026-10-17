package com.example.ballgame

import android.graphics.*

class UI {
    val leftButton = RectF()
    val rightButton = RectF()
    val jumpButton = RectF()
    val resetButton = RectF()
    val pauseButton = RectF()

    val playButton = RectF()
    val backButton = RectF()
    val levelButtons = mutableListOf<RectF>()

    val resumeButton = RectF()
    val menuButton = RectF()

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 40, 70, 90) }
    private val jumpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(210, 200, 50, 50) }
    private val resetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 60, 80, 100) }
    private val menuBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(230, 230, 90, 60) }

    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(6f, 2f, 2f, Color.BLACK)
    }

    fun updateButtons(screenW: Int, screenH: Int) {
        val btnSize = screenH * 0.24f
        val margin = btnSize * 0.15f
        val y = screenH - btnSize - margin

        leftButton.set(margin, y, margin + btnSize, y + btnSize)
        rightButton.set(margin * 2 + btnSize, y, margin * 2 + btnSize * 2, y + btnSize)
        jumpButton.set(screenW - btnSize - margin, y, screenW - margin, y + btnSize)

        val smallBtn = btnSize * 0.55f
        pauseButton.set(margin, margin, margin + smallBtn, margin + smallBtn)

        val resetSize = smallBtn
        resetButton.set(screenW - resetSize - margin, margin,
            screenW - margin, margin + resetSize)

        val pw = screenW * 0.35f
        val ph = screenH * 0.15f
        playButton.set((screenW - pw) / 2f, screenH * 0.62f,
            (screenW + pw) / 2f, screenH * 0.62f + ph)

        resumeButton.set((screenW - pw) / 2f, screenH * 0.42f,
            (screenW + pw) / 2f, screenH * 0.42f + ph)
        menuButton.set((screenW - pw) / 2f, screenH * 0.60f,
            (screenW + pw) / 2f, screenH * 0.60f + ph)

        backButton.set(margin * 2, margin * 2,
            margin * 2 + smallBtn, margin * 2 + smallBtn * 0.8f)
    }

    fun buildLevelButtons(screenW: Int, screenH: Int, highestUnlocked: Int) {
        levelButtons.clear()
        val cols = 5
        val rows = 4
        val padding = screenW * 0.04f
        val topOffset = screenH * 0.18f
        val bottomOffset = screenH * 0.05f
        val gridW = screenW - padding * 2
        val gridH = screenH - topOffset - bottomOffset
        val cellW = gridW / cols
        val cellH = gridH / rows
        val btnPadding = cellW * 0.08f

        for (i in 0 until 20) {
            val col = i % cols
            val row = i / cols
            val x = padding + col * cellW + btnPadding
            val y = topOffset + row * cellH + btnPadding
            levelButtons.add(RectF(x, y, x + cellW - btnPadding * 2, y + cellH - btnPadding * 2))
        }
    }

    fun drawMenu(canvas: Canvas, screenW: Int, screenH: Int) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 90, 80)
            textSize = screenH * 0.15f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(18f, 5f, 5f, Color.BLACK)
        }
        canvas.drawText("RED BALL", screenW / 2f, screenH * 0.35f, titlePaint)

        val subPaint = Paint(titlePaint).apply {
            color = Color.rgb(255, 200, 100)
            textSize = screenH * 0.055f
        }
        canvas.drawText("20 LEVELS OF CHALLENGE", screenW / 2f, screenH * 0.47f, subPaint)

        canvas.drawRoundRect(playButton, 30f, 30f, menuBtnPaint)
        val btnTxt = Paint(txtPaint).apply { textSize = screenH * 0.075f }
        canvas.drawText("▶  PLAY", playButton.centerX(), playButton.centerY() + screenH * 0.028f, btnTxt)

        val stars = SaveManager.getTotalStars()
        val starPaint = Paint(txtPaint).apply {
            color = Color.rgb(255, 220, 80)
            textSize = screenH * 0.05f
        }
        canvas.drawText("⭐ $stars / 20", screenW / 2f, screenH * 0.92f, starPaint)
    }

    fun drawLevelSelect(canvas: Canvas, screenW: Int, screenH: Int) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 200, 100)
            textSize = screenH * 0.09f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(10f, 3f, 3f, Color.BLACK)
        }
        canvas.drawText("SELECT LEVEL", screenW / 2f, screenH * 0.12f, titlePaint)

        val highest = SaveManager.getHighestUnlocked()

        for (i in levelButtons.indices) {
            val rect = levelButtons[i]
            val levelNum = i + 1
            val unlocked = levelNum <= highest

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (unlocked) {
                val completed = SaveManager.getBestScore(levelNum) >= 100
                bgPaint.color = if (completed) Color.rgb(255, 200, 60) else Color.rgb(80, 130, 180)
            } else {
                bgPaint.color = Color.rgb(50, 50, 60)
            }
            canvas.drawRoundRect(rect, 18f, 18f, bgPaint)

            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (unlocked) Color.WHITE else Color.rgb(80, 80, 90)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(rect, 18f, 18f, border)

            val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (unlocked) Color.WHITE else Color.rgb(180, 180, 180)
                textSize = rect.height() * 0.55f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                setShadowLayer(4f, 2f, 2f, Color.BLACK)
            }
            val textY = rect.centerY() + numPaint.textSize * 0.35f
            if (unlocked) {
                canvas.drawText("$levelNum", rect.centerX(), textY, numPaint)
            } else {
                canvas.drawText("🔒", rect.centerX(), textY, numPaint)
            }
        }

        canvas.drawRoundRect(backButton, 15f, 15f, resetPaint)
        val backPaint = Paint(txtPaint).apply { textSize = backButton.height() * 0.65f }
        canvas.drawText("◀", backButton.centerX(), backButton.centerY() + backButton.height() * 0.22f, backPaint)
    }

    fun drawGameUI(canvas: Canvas, player: Player, screenW: Int, screenH: Int) {
        val smallTxt = Paint(txtPaint).apply { textSize = screenH * 0.06f }

        canvas.drawRoundRect(leftButton, 25f, 25f, btnPaint)
        canvas.drawText("◀", leftButton.centerX(), leftButton.centerY() + smallTxt.textSize * 0.35f, smallTxt)

        canvas.drawRoundRect(rightButton, 25f, 25f, btnPaint)
        canvas.drawText("▶", rightButton.centerX(), rightButton.centerY() + smallTxt.textSize * 0.35f, smallTxt)

        canvas.drawRoundRect(jumpButton, 25f, 25f, jumpPaint)
        canvas.drawText("⬆", jumpButton.centerX(), jumpButton.centerY() + smallTxt.textSize * 0.35f, smallTxt)

        canvas.drawRoundRect(pauseButton, 15f, 15f, resetPaint)
        val pauseTxt = Paint(txtPaint).apply { textSize = pauseButton.height() * 0.55f }
        canvas.drawText("❚❚", pauseButton.centerX(),
            pauseButton.centerY() + pauseTxt.textSize * 0.35f, pauseTxt)

        canvas.drawRoundRect(resetButton, 15f, 15f, resetPaint)
        val resetTxt = Paint(txtPaint).apply { textSize = resetButton.height() * 0.65f }
        canvas.drawText("⟲", resetButton.centerX(),
            resetButton.centerY() + resetTxt.textSize * 0.35f, resetTxt)

        val statSize = screenH * 0.05f
        val statPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = statSize
            isFakeBoldText = true
            setShadowLayer(6f, 2f, 2f, Color.BLACK)
        }

        val topY = pauseButton.centerY() + statSize * 0.35f

        statPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("⭐ ${GameState.score}",
            pauseButton.right + screenW * 0.02f, topY, statPaint)

        val lvlPaint = Paint(statPaint).apply {
            color = Color.rgb(255, 190, 60)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("LEVEL ${GameState.currentLevel}", screenW / 2f, topY, lvlPaint)

        statPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("❤ ${GameState.lives}",
            resetButton.left - screenW * 0.02f, topY, statPaint)

        if (GameState.levelComplete) {
            canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(),
                Paint().apply { color = Color.argb(200, 0, 0, 0) })
            val big = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(255, 220, 80)
                textSize = screenH * 0.18f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                setShadowLayer(14f, 4f, 4f, Color.BLACK)
            }
            canvas.drawText("LEVEL CLEAR!", screenW / 2f, screenH / 2f - 20f, big)
            val sub = Paint(big).apply { textSize = screenH * 0.09f; color = Color.WHITE }
            canvas.drawText("+100 points", screenW / 2f, screenH / 2f + 60f, sub)
        }

        if (GameState.gameOver) {
            canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(),
                Paint().apply { color = Color.argb(220, 0, 0, 0) })
            val big = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = screenH * 0.18f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                setShadowLayer(14f, 4f, 4f, Color.BLACK)
            }
            if (GameState.won) {
                big.color = Color.rgb(255, 220, 80)
                canvas.drawText("YOU WIN!", screenW / 2f, screenH / 2f - 30f, big)
                val sub = Paint(big).apply { textSize = screenH * 0.08f; color = Color.WHITE }
                canvas.drawText("All 20 levels cleared!", screenW / 2f, screenH / 2f + 40f, sub)
            } else {
                big.color = Color.rgb(255, 60, 60)
                canvas.drawText("GAME OVER", screenW / 2f, screenH / 2f - 30f, big)
                val sub = Paint(big).apply { textSize = screenH * 0.09f; color = Color.WHITE }
                canvas.drawText("Score: ${GameState.score}", screenW / 2f, screenH / 2f + 40f, sub)
            }
            val hint = Paint(big).apply { textSize = screenH * 0.06f; color = Color.rgb(180, 210, 255) }
            canvas.drawText("Tap ⟲ to retry", screenW / 2f, screenH / 2f + 120f, hint)
        }
    }

    // NEW: Respawn countdown overlay
    fun drawRespawnOverlay(canvas: Canvas, screenW: Int, screenH: Int, timeLeft: Float) {
        // Semi-transparent red tint
        val alpha = (60 + (Math.sin(timeLeft * 20.0) * 20)).toInt().coerceIn(1, 255)
        canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(),
            Paint().apply { color = Color.argb(alpha, 80, 0, 0) })

        val centerX = screenW / 2f
        val centerY = screenH / 2f

        // "READY" text
        val big = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 90, 60)
            textSize = screenH * 0.20f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(18f, 5f, 5f, Color.BLACK)
        }
        canvas.drawText("READY", centerX, centerY - screenH * 0.05f, big)

        // Countdown number
        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 220, 80)
            textSize = screenH * 0.28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(20f, 6f, 6f, Color.BLACK)
        }
        val count = (timeLeft / 0.5f).toInt() + 1
        canvas.drawText("$count", centerX, centerY + screenH * 0.20f, countPaint)
    }

    fun drawPauseMenu(canvas: Canvas, screenW: Int, screenH: Int) {
        canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(),
            Paint().apply { color = Color.argb(210, 0, 0, 0) })

        val titlePaint = Paint(txtPaint).apply {
            textSize = screenH * 0.15f
            color = Color.WHITE
        }
        canvas.drawText("PAUSED", screenW / 2f, screenH * 0.28f, titlePaint)

        canvas.drawRoundRect(resumeButton, 25f, 25f, menuBtnPaint)
        val btnTxt = Paint(txtPaint).apply { textSize = screenH * 0.07f }
        canvas.drawText("▶ RESUME", resumeButton.centerX(), resumeButton.centerY() + screenH * 0.025f, btnTxt)

        val menuBtnPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(80, 100, 120) }
        canvas.drawRoundRect(menuButton, 25f, 25f, menuBtnPaint2)
        canvas.drawText("☰ MENU", menuButton.centerX(), menuButton.centerY() + screenH * 0.025f, btnTxt)
    }
}