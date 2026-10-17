package com.example.ballgame

import android.graphics.Canvas
import android.graphics.Paint

class Enemy(
    var x: Float, var y: Float,
    val minX: Float, val maxX: Float,
    val speed: Float
) {
    var vx = speed
    var alive = true
    val radius = 20f

    private fun cRgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    // No anti-aliasing for speed
    private val bodyPaint = Paint().apply { color = cRgb(200, 60, 40); isAntiAlias = false }
    private val eyePaint = Paint().apply { color = cRgb(255, 255, 255); isAntiAlias = false }
    private val pupilPaint = Paint().apply { color = cRgb(0, 0, 0); isAntiAlias = false }

    fun update(dt: Float) {
        x += vx * dt
        if (x > maxX) { x = maxX; vx = -speed }
        if (x < minX) { x = minX; vx = speed }
    }

    fun draw(canvas: Canvas) {
        // Simple 2-circle enemy — much faster than before
        canvas.drawCircle(x, y, radius, bodyPaint)
        canvas.drawCircle(x - 6f, y - 5f, 4f, eyePaint)
        canvas.drawCircle(x + 6f, y - 5f, 4f, eyePaint)
        val look = if (vx > 0) 2f else -2f
        canvas.drawCircle(x - 6f + look, y - 5f, 2f, pupilPaint)
        canvas.drawCircle(x + 6f + look, y - 5f, 2f, pupilPaint)
    }
}