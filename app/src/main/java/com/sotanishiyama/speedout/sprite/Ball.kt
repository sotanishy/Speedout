package com.sotanishiyama.speedout.sprite

import android.graphics.Color
import com.sotanishiyama.speedout.GameView
import java.util.Random

class Ball(gameView: GameView) : Sprite(gameView) {

    var dx: Double = 0.0
        private set
    var dy: Double = 0.0
        private set

    var speed: Double = 0.0
        private set

    var isMoving: Boolean = false

    init {

        x = 0.0
        y = 0.0
        width = (40 * gameView.scaleX).toInt()
        height = width
        color = Color.LTGRAY

        reset()
    }

    fun reset() {
        speed = 15 * gameView.scaleX
        setDirection(Random().nextDouble() * 2 - 1.0, -1.0)
        isMoving = false
    }

    fun setDirection(dx: Double, dy: Double) {
        // normalize the velocity vector
        val d = Math.sqrt(dx * dx + dy * dy)
        this.dx = dx / d
        this.dy = dy / d
    }

    fun increaseSpeed(percentage: Int) {
        speed *= 1 + percentage.toDouble() / 100
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED
        }
    }

    companion object {
        private const val MAX_SPEED = 10000.0
    }
}
