package com.sotanishiyama.speedout.sprite

import android.graphics.Color
import com.sotanishiyama.speedout.GameView

class Brick(gameView: GameView, r: Int, c: Int, color: Int) : Sprite(gameView) {

    var isDestroyed: Boolean = false

    init {

        this.width = gameView.screenWidth / COLUMNS
        this.height = (50 * gameView.scaleY).toInt()

        val yOffset = 300 * gameView.scaleY
        this.x = (width * c).toDouble()
        this.y = height * r + yOffset

        this.color = color
    }

    companion object {

        const val ROWS = 3
        const val COLUMNS = 6
        const val NUMS = ROWS * COLUMNS

        val RED = Color.argb(255, 255, 102, 102)
        val ORANGE = Color.argb(255, 255, 178, 102)
        val YELLOW = Color.argb(255, 255, 255, 102)
        val GREEN = Color.argb(255, 102, 255, 102)
        val BLUE = Color.argb(255, 102, 178, 255)
        val PURPLE = Color.argb(255, 255, 102, 255)
    }
}
