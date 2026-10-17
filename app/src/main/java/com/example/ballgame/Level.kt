package com.example.ballgame

import android.graphics.*
import kotlin.math.hypot
import kotlin.random.Random

class Level {
    var width = 2200f
    var currentLevel = 1
    val platforms = mutableListOf<RectF>()
    val slopes = mutableListOf<Slope>()
    val spikes = mutableListOf<RectF>()
    val enemies = mutableListOf<Enemy>()
    var portal = RectF()

    // Slope = a tilted platform. Ball slides on it.
    class Slope(
        val x1: Float, val y1: Float,   // left end
        val x2: Float, val y2: Float,   // right end
        val thickness: Float = 20f
    ) {
        // Returns the y of the slope surface at given x (null if outside)
        fun surfaceY(x: Float): Float? {
            if (x < x1 || x > x2) return null
            val t = (x - x1) / (x2 - x1)
            return y1 + (y2 - y1) * t
        }
        // Angle of the slope in radians
        fun angle(): Float = kotlin.math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()
    }

    private fun cRgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun cArgb(a: Int, r: Int, g: Int, b: Int): Int =
        ((a.coerceIn(1, 255)) shl 24) or (r shl 16) or (g shl 8) or b

    // Pre-created Paints
    private val grassTop = Paint().apply { color = cRgb(90, 160, 60); isAntiAlias = false }
    private val grassDark = Paint().apply { color = cRgb(60, 120, 40); isAntiAlias = false }
    private val dirtMid = Paint().apply { color = cRgb(120, 85, 50); isAntiAlias = false }
    private val dirtDark = Paint().apply { color = cRgb(80, 55, 32); isAntiAlias = false }
    private val dirtLight = Paint().apply { color = cRgb(145, 105, 65); isAntiAlias = false }
    private val woodLine = Paint().apply {
        color = cArgb(120, 60, 40, 20)
        strokeWidth = 2f
        isAntiAlias = false
    }
    private val spikePaint = Paint().apply { color = cRgb(220, 40, 40); isAntiAlias = false }
    private val spikeDark = Paint().apply { color = cRgb(130, 15, 15); isAntiAlias = false }
    private val spikeShine = Paint().apply { color = cRgb(255, 120, 120); isAntiAlias = false }
    private val portalOuter = Paint().apply { color = cRgb(255, 200, 60); isAntiAlias = false }
    private val portalMid = Paint().apply { color = cRgb(255, 240, 150); isAntiAlias = false }
    private val portalInner = Paint().apply { color = cRgb(255, 255, 230); isAntiAlias = false }

    private val FLOOR_Y = 340f

    fun loadLevel(n: Int) {
        currentLevel = n
        platforms.clear(); slopes.clear(); spikes.clear(); enemies.clear()

        val random = Random(n * 7919)
        width = (5000f + n * 400f).coerceAtMost(14000f)

        // ============ EASY: WIDE GROUND SEGMENTS ============
        val segCount = (width / 500f).toInt().coerceIn(10, 30)
        val segW = width / segCount
        for (i in 0 until segCount) {
            val segX = i * segW
            // EASY: 85-100% of the slot (small gaps only)
            val actualW = segW * (0.85f + random.nextFloat() * 0.15f)
            addGround(segX, actualW)
        }

        // ============ CURVES / SLOPES ============
        // Add a curve every ~900px — like the original Red Ball
        var curveX = 600f
        while (curveX < width - 800f) {
            val curveType = random.nextInt(0, 3)
            val curveWidth = 200f + random.nextFloat() * 180f

            when (curveType) {
                0 -> {
                    // Ramp going UP (hill)
                    slopes.add(Slope(curveX, FLOOR_Y, curveX + curveWidth, FLOOR_Y - 90f))
                    // Flat top
                    addPlat(curveX + curveWidth - 10f, FLOOR_Y - 90f - 20f, 100f)
                    // Down ramp
                    slopes.add(Slope(curveX + curveWidth + 90f, FLOOR_Y - 90f, curveX + curveWidth + 90f + curveWidth * 0.7f, FLOOR_Y))
                }
                1 -> {
                    // Valley (down then up)
                    slopes.add(Slope(curveX, FLOOR_Y, curveX + curveWidth * 0.5f, FLOOR_Y + 60f))
                    slopes.add(Slope(curveX + curveWidth * 0.5f, FLOOR_Y + 60f, curveX + curveWidth, FLOOR_Y))
                }
                2 -> {
                    // Straight down-ramp
                    slopes.add(Slope(curveX, FLOOR_Y - 80f, curveX + curveWidth, FLOOR_Y))
                }
            }
            curveX += curveWidth + 500f + random.nextFloat() * 400f
        }

        // ============ FLOATING PLATFORMS (wooden) ============
        val platCount = (width / 500f).toInt().coerceIn(10, 30)
        for (i in 0 until platCount) {
            val px = 350f + i * (width - 600f) / platCount + random.nextInt(-60, 60)
            val py = 150f + random.nextInt(-30, 100)
            val pw = 90f + random.nextInt(0, 60)
            addPlat(px.coerceIn(100f, width - 200f), py, pw)
        }

        // ============ SPIKES (EASY: fewer) ============
        val spikeCount = (width / 900f).toInt().coerceIn(4, 16)
        for (i in 0 until spikeCount) {
            val sx = 700f + i * (width - 1200f) / spikeCount + random.nextInt(-60, 60)
            val sw = 50 + random.nextInt(0, 40)
            if (sx + sw < width - 400f) {
                addSpike(sx.coerceAtLeast(300f), sw.toFloat())
            }
        }

        // ============ ENEMIES (EASY: fewer + slower) ============
        val enemyCount = (width / 900f).toInt().coerceIn(3, 14)
        for (i in 0 until enemyCount) {
            val ex = 800f + i * (width - 1400f) / enemyCount + random.nextInt(-60, 60)
            val range = 60f + random.nextFloat() * 40f
            val minX = (ex - range).coerceAtLeast(0f)
            val maxX = (ex + range).coerceAtMost(width)
            val speed = 60f + n * 3f + random.nextInt(-10, 15)
            addEnemy(ex.coerceIn(250f, width - 300f), minX, maxX, speed)
        }

        // Portal at end
        portal = RectF(width - 130f, FLOOR_Y - 120f, width - 40f, FLOOR_Y)
    }

    private fun addGround(x: Float, w: Float) {
        platforms.add(RectF(x, FLOOR_Y, x + w, FLOOR_Y + 60f))
    }
    private fun addPlat(x: Float, y: Float, w: Float) {
        platforms.add(RectF(x, y, x + w, y + 20f))
    }
    private fun addSpike(x: Float, w: Float) {
        spikes.add(RectF(x, FLOOR_Y - 22f, x + w, FLOOR_Y))
    }
    private fun addEnemy(x: Float, minX: Float, maxX: Float, speed: Float) {
        enemies.add(Enemy(x, FLOOR_Y - 20f, minX, maxX, speed))
    }

    fun updateEnemies(dt: Float) { for (e in enemies) e.update(dt) }

    // ---- COLLISION HELPERS ----
    fun collidesHorizontally(nx: Float, ny: Float, r: Float): Boolean {
        for (p in platforms) if (nx + r > p.left && nx - r < p.right &&
            ny + r > p.top + 2f && ny - r < p.bottom - 2f) return true
        return false
    }
    fun collidesVertically(nx: Float, ny: Float, r: Float): Boolean {
        for (p in platforms) if (nx + r > p.left && nx - r < p.right &&
            ny + r > p.top && ny - r < p.bottom) return true
        // Slopes
        for (s in slopes) {
            val sy = s.surfaceY(nx) ?: continue
            if (ny + r > sy && ny + r < sy + s.thickness + 20f) return true
        }
        return false
    }
    fun collidesFromBelow(nx: Float, ny: Float, r: Float): Boolean {
        for (p in platforms) if (nx + r > p.left && nx - r < p.right &&
            ny - r < p.bottom && ny - r > p.top - 5f && ny + r > p.top) return true
        return false
    }
    fun getGroundY(nx: Float, ny: Float, r: Float): Float? {
        var bestY: Float? = null
        for (p in platforms) if (nx + r > p.left && nx - r < p.right &&
            ny + r >= p.top && ny + r <= p.top + 30f) {
            if (bestY == null || p.top < bestY) bestY = p.top
        }
        // Slopes
        for (s in slopes) {
            val sy = s.surfaceY(nx) ?: continue
            if (ny + r >= sy - 5f && ny + r <= sy + 40f) {
                if (bestY == null || sy < bestY) bestY = sy
            }
        }
        return bestY
    }

    fun checkSpikeCollision(player: Player): Boolean {
        if (player.invulnerable > 0f) return false
        for (s in spikes) if (player.x + player.radius > s.left + 3f &&
            player.x - player.radius < s.right - 3f &&
            player.y + player.radius > s.top + 3f &&
            player.y - player.radius < s.bottom) return true
        return false
    }

    fun checkEnemyCollision(player: Player, onScore: (Int) -> Unit) {
        for (e in enemies) {
            if (!e.alive) continue
            val dist = hypot(player.x - e.x, player.y - e.y)
            if (dist < player.radius + e.radius) {
                if (player.vy > 0f && player.y < e.y - 5f) {
                    e.alive = false
                    player.vy = -500f
                    onScore(30)
                } else if (player.invulnerable <= 0f) {
                    player.hit()
                    if (GameState.lives <= 0) GameState.gameOver = true else player.reset()
                }
            }
        }
    }

    fun checkPortalCollision(player: Player): Boolean {
        return player.x + player.radius > portal.left && player.x - player.radius < portal.right &&
                player.y + player.radius > portal.top && player.y - player.radius < portal.bottom
    }

    // ==================== DRAW ====================
    fun draw(canvas: Canvas, camera: Camera, screenW: Int) {
        val leftBound = camera.x - 100f
        val rightBound = camera.x + screenW + 100f

        // ---- GROUND PLATFORMS (grass + dirt texture) ----
        for (p in platforms) {
            if (p.right < leftBound || p.left > rightBound) continue
            drawPlatformTexture(canvas, p)
        }

        // ---- SLOPES (curves) ----
        for (s in slopes) {
            if (s.x2 < leftBound || s.x1 > rightBound) continue
            drawSlope(canvas, s)
        }

        // ---- SPIKES ----
        for (s in spikes) {
            if (s.right < leftBound || s.left > rightBound) continue
            val count = ((s.right - s.left) / 22f).toInt()
            if (count <= 0) continue
            val w = (s.right - s.left) / count
            for (i in 0 until count) {
                val sx = s.left + i * w
                // Body
                canvas.drawRect(sx, s.top + 4f, sx + w, s.bottom, spikeDark)
                // Triangle tip
                canvas.drawRect(sx + w * 0.25f, s.top - 4f, sx + w * 0.75f, s.top + 4f, spikePaint)
                // Shine
                canvas.drawRect(sx + w * 0.4f, s.top - 2f, sx + w * 0.6f, s.top + 1f, spikeShine)
            }
        }

        // ---- ENEMIES ----
        for (e in enemies) {
            if (!e.alive) continue
            if (e.x < leftBound || e.x > rightBound) continue
            e.draw(canvas)
        }

        // ---- PORTAL ----
        if (portal.right >= leftBound && portal.left <= rightBound) {
            canvas.drawRect(portal, portalOuter)
            canvas.drawRect(portal.left + 12f, portal.top + 12f,
                portal.right - 12f, portal.bottom - 12f, portalMid)
            canvas.drawRect(portal.left + 25f, portal.top + 25f,
                portal.right - 25f, portal.bottom - 25f, portalInner)
        }
    }

    // Texture for normal platforms: grass top + dirt body with brick pattern
    private fun drawPlatformTexture(canvas: Canvas, p: RectF) {
        // Dirt body
        canvas.drawRect(p, dirtMid)

        // Darker bottom half
        canvas.drawRect(p.left, p.top + p.height() * 0.5f, p.right, p.bottom, dirtDark)

        // Brick pattern (vertical lines every 30px)
        var bx = p.left
        while (bx < p.right) {
            canvas.drawLine(bx, p.top + 18f, bx, p.bottom, woodLine)
            bx += 30f
        }
        // Horizontal line
        canvas.drawLine(p.left, p.top + p.height() * 0.5f, p.right, p.top + p.height() * 0.5f, woodLine)

        // Grass on top
        canvas.drawRect(p.left, p.top, p.right, p.top + 10f, grassTop)
        canvas.drawRect(p.left, p.top + 10f, p.right, p.top + 16f, grassDark)

        // Grass tufts (small triangles on top)
        var gx = p.left + 8f
        while (gx < p.right - 4f) {
            canvas.drawRect(gx, p.top - 4f, gx + 3f, p.top, grassTop)
            canvas.drawRect(gx + 6f, p.top - 6f, gx + 9f, p.top, grassDark)
            gx += 20f
        }
    }

    // Draw a slope with the same texture style
    private fun drawSlope(canvas: Canvas, s: Slope) {
        val angle = s.angle()
        val length = hypot((s.x2 - s.x1).toDouble(), (s.y2 - s.y1).toDouble()).toFloat()

        canvas.save()
        canvas.translate(s.x1, s.y1)
        canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())

        // Dirt body
        canvas.drawRect(0f, 0f, length, s.thickness + 40f, dirtMid)
        canvas.drawRect(0f, (s.thickness + 40f) / 2, length, s.thickness + 40f, dirtDark)

        // Brick lines
        var bx = 0f
        while (bx < length) {
            canvas.drawLine(bx, 18f, bx, s.thickness + 40f, woodLine)
            bx += 30f
        }

        // Grass top
        canvas.drawRect(0f, 0f, length, 10f, grassTop)
        canvas.drawRect(0f, 10f, length, 16f, grassDark)

        // Grass tufts
        var gx = 8f
        while (gx < length - 4f) {
            canvas.drawRect(gx, -4f, gx + 3f, 0f, grassTop)
            gx += 20f
        }

        canvas.restore()
    }
}