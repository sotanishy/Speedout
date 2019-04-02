package com.sotanishiyama.speedout.sprite

import android.graphics.Color
import com.sotanishiyama.speedout.GameView

class Paddle(gameView: GameView) : Sprite(gameView) {

    init {

        width = (200 * gameView.scaleX).toInt()
        height = (30 * gameView.scaleY).toInt()

        setCenterX(gameView.screenWidth / 2.0)
        y = gameView.screenHeight * .9

        color = Color.LTGRAY
    }

    fun setCenterX(centerX: Double) {
        var newX = centerX - width / 2
        if (newX < 0)
            newX = 0.0
        else if (newX + width > gameView.screenWidth)
            newX = (gameView.screenWidth - width).toDouble()
        x = newX
    }
}
