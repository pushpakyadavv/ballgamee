package com.example.ballgame

class Camera {
    var x = 0f
    var y = 0f

    fun followPlayer(player: Player, levelWidth: Float, screenW: Int, screenH: Int,
                     levelHeight: Float = 900f, zoom: Float = 1.0f) {
        // Effective visible width in world units (after zoom)
        val viewW = screenW / zoom
        val viewH = screenH / zoom

        // Horizontal follow — player sits at 30% from left edge
        val targetX = player.x - viewW * 0.30f
        x += (targetX - x) * 0.20f
        if (x < 0f) x = 0f
        val maxX = levelWidth - viewW
        if (x > maxX) x = maxX

        // Vertical follow — player centered vertically
        val targetY = player.y - viewH * 0.55f
        y += (targetY - y) * 0.12f
        if (y < -100f) y = -100f
        val maxY = levelHeight - viewH
        if (y > maxY) y = maxY
        if (y < 0f) y = 0f
    }
}