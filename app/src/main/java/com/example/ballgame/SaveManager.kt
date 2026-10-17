package com.example.ballgame

import android.content.Context
import android.content.SharedPreferences

object SaveManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("red_ball_save", Context.MODE_PRIVATE)
    }

    fun getHighestUnlocked(): Int = prefs.getInt("highest_unlocked", 1)

    fun setHighestUnlocked(level: Int) {
        if (level > getHighestUnlocked()) {
            prefs.edit().putInt("highest_unlocked", level).apply()
        }
    }

    fun getBestScore(level: Int): Int = prefs.getInt("score_$level", 0)

    fun setBestScore(level: Int, score: Int) {
        if (score > getBestScore(level)) {
            prefs.edit().putInt("score_$level", score).apply()
        }
    }

    fun getTotalStars(): Int {
        var total = 0
        for (i in 1..20) {
            if (getBestScore(i) >= 100) total++
        }
        return total
    }
}