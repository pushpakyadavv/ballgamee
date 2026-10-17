package com.example.ballgame

import android.graphics.*

class Player {
    var x = 100f
    var y = 280f
    var vx = 0f
    var vy = 0f
    val radius = 22f
    var onGround = false
    var invulnerable = 0f

    private fun cRgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun cArgb(a: Int, r: Int, g: Int, b: Int): Int =
        ((a.coerceIn(0, 255)) shl 24) or (r shl 16) or (g shl 8) or b

    private val outlinePaint = Paint().apply {
        color = cRgb(180, 30, 40)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = false
    }
    private val highlightPaint = Paint().apply {
        color = cArgb(220, 255, 255, 255)
        isAntiAlias = false
    }
    private val ballPaint = Paint().apply {
        color = cRgb(230, 40, 50)
        isAntiAlias = false
    }
    private val ballShade = Paint().apply {
        color = cArgb(80, 100, 0, 0)
        isAntiAlias = false
    }

    fun reset() {
        x = 100f; y = 280f; vx = 0f; vy = 0f; invulnerable = 1.0f
    }

    fun hit() {
        if (invulnerable > 0f) return
        GameState.lives--
        invulnerable = 1.5f
    }

    fun update(dt: Float, left: Boolean, right: Boolean, jump: Boolean,
               level: Level, screenW: Int, screenH: Int) {

        if (invulnerable > 0f) invulnerable -= dt

        val speed = 420f
        val gravity = 1400f
        val jumpPower = -720f

        if (left) vx = -speed else if (right) vx = speed else vx *= 0.75f

        if (jump && onGround) { vy = jumpPower; onGround = false }

        vy += gravity * dt
        if (vy > 1300f) vy = 1300f

        // --- Horizontal movement + slope snapping ---
        val newX = x + vx * dt
        if (!level.collidesHorizontally(newX, y, radius)) {
            x = newX
        } else {
            vx = 0f
        }

        // --- Vertical movement ---
        val newY = y + vy * dt
        onGround = false

        if (vy >= 0f) {
            // Falling — check normal platforms + slopes
            var landed = false

            // Slopes first (higher priority)
            for (s in level.slopes) {
                val sy = s.surfaceY(x) ?: continue
                if (newY + radius >= sy && y + radius <= sy + 30f) {
                    y = sy - radius
                    vy = 0f
                    onGround = true
                    landed = true
                    // Slide down the slope
                    val slide = kotlin.math.sin(s.angle().toDouble()).toFloat() * 80f
                    vx += slide * (s.y2 - s.y1) / 100f
                    break
                }
            }

            if (!landed) {
                if (level.collidesVertically(x, newY, radius)) {
                    val gY = level.getGroundY(x, y, radius)
                    if (gY != null) { y = gY - radius; vy = 0f; onGround = true }
                    else y = newY
                } else y = newY
            }
        } else {
            if (level.collidesFromBelow(x, newY, radius)) vy = 0f else y = newY
        }

        if (x - radius < 0) { x = radius; vx = 0f }
        if (x + radius > level.width) { x = level.width - radius; vx = 0f }
    }

    fun draw(canvas: Canvas) {
        if (invulnerable > 0f && (invulnerable * 12).toInt() % 2 == 0) return
        canvas.save()
        canvas.translate(x, y)
        // Shadow at bottom
        canvas.drawCircle(2f, 4f, radius, ballShade)
        // Main ball
        canvas.drawCircle(0f, 0f, radius, ballPaint)
        // Highlight
        canvas.drawCircle(-6f, -6f, radius * 0.35f, highlightPaint)
        // Outline
        canvas.drawCircle(0f, 0f, radius, outlinePaint)
        canvas.restore()
    }
}