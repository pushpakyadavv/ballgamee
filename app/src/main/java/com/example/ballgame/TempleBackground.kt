package com.example.ballgame

import android.graphics.*

class TempleBackground {
    private var screenW = 0
    private var screenH = 0

    private fun cRgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun cArgb(a: Int, r: Int, g: Int, b: Int): Int =
        ((a.coerceIn(1, 255)) shl 24) or (r shl 16) or (g shl 8) or b

    // All Paints created ONCE at construction
    private val bgTop = Paint().apply { color = cRgb(58, 42, 26); isAntiAlias = false }
    private val bgMid = Paint().apply { color = cRgb(42, 30, 18); isAntiAlias = false }
    private val bgBottom = Paint().apply { color = cRgb(25, 16, 10); isAntiAlias = false }
    private val wallPaint = Paint().apply { color = cRgb(90, 66, 44); isAntiAlias = false }
    private val wallDark = Paint().apply { color = cRgb(66, 46, 28); isAntiAlias = false }
    private val seamPaint = Paint().apply {
        color = cArgb(100, 20, 12, 6)
        strokeWidth = 3f
        isAntiAlias = false
    }
    private val rayPaint = Paint().apply { color = cArgb(40, 255, 220, 150); isAntiAlias = false }
    private val vignettePaint = Paint().apply { color = cArgb(120, 0, 0, 0); isAntiAlias = false }
    private val overlayPaint = Paint().apply { color = cArgb(70, 10, 5, 2); isAntiAlias = false }

    fun setScreen(w: Int, h: Int) {
        screenW = w
        screenH = h
    }

    fun draw(canvas: Canvas, cameraX: Float) {
        // Guard: if screen size not set yet, skip drawing
        if (screenW <= 0 || screenH <= 0) return

        // ==========================================
        // 1) FULL-SCREEN BACKGROUND — 3 horizontal bands
        // ==========================================
        val bandH = screenH / 3f
        canvas.drawRect(0f, 0f, screenW.toFloat(), bandH, bgTop)
        canvas.drawRect(0f, bandH, screenW.toFloat(), bandH * 2f, bgMid)
        canvas.drawRect(0f, bandH * 2f, screenW.toFloat(), screenH.toFloat(), bgBottom)

        // ==========================================
        // 2) STONE WALL PANELS (parallax)
        // ==========================================
        canvas.save()
        canvas.translate(-cameraX * 0.3f, 0f)
        val panelW = (screenW * 0.5f).toInt().coerceAtLeast(250)
        val start = ((cameraX * 0.3f) / panelW).toInt() * panelW - panelW
        var wx = start
        while (wx < start + screenW + panelW * 2) {
            val x = wx.toFloat()
            val w = panelW.toFloat()

            // Main wall
            canvas.drawRect(x, 0f, x + w, screenH.toFloat(), wallPaint)

            // Darker bottom
            canvas.drawRect(x, screenH * 0.75f, x + w, screenH.toFloat(), wallDark)

            // Vertical seams
            canvas.drawLine(x, 0f, x, screenH.toFloat(), seamPaint)
            canvas.drawLine(x + w, 0f, x + w, screenH.toFloat(), seamPaint)

            // Horizontal seams
            canvas.drawLine(x, screenH * 0.25f, x + w, screenH * 0.25f, seamPaint)
            canvas.drawLine(x, screenH * 0.50f, x + w, screenH * 0.50f, seamPaint)
            canvas.drawLine(x, screenH * 0.75f, x + w, screenH * 0.75f, seamPaint)

            wx += panelW
        }
        canvas.restore()

        // ==========================================
        // 3) DARK OVERLAY
        // ==========================================
        canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), overlayPaint)

        // ==========================================
        // 4) SUN RAYS — 2 rays
        // ==========================================
        drawRay(canvas, screenW * 0.20f, screenW * 0.09f, 0.15f)
        drawRay(canvas, screenW * 0.70f, screenW * 0.09f, -0.12f)

        // ==========================================
        // 5) SIDE VIGNETTES
        // ==========================================
        canvas.drawRect(0f, 0f, screenW * 0.05f, screenH.toFloat(), vignettePaint)
        canvas.drawRect(screenW * 0.95f, 0f, screenW.toFloat(), screenH.toFloat(), vignettePaint)
    }

    private fun drawRay(canvas: Canvas, baseX: Float, width: Float, angle: Float) {
        val h = screenH.toFloat()
        val dx = angle * h
        val path = Path()
        path.moveTo(baseX, 0f)
        path.lineTo(baseX + width, 0f)
        path.lineTo(baseX + width + dx, h)
        path.lineTo(baseX + dx, h)
        path.close()
        canvas.drawPath(path, rayPaint)
    }
}