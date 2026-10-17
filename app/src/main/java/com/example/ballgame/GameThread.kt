package com.example.ballgame

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(
    private val holder: SurfaceHolder,
    private val view: GameView
) : Thread() {

    @Volatile private var running = false

    fun setRunning(run: Boolean) { running = run }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            var dt = (now - lastTime) / 1_000_000_000f
            lastTime = now
            if (dt > 0.05f) dt = 0.05f

            view.update(dt)

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    synchronized(holder) { view.render(canvas) }
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}