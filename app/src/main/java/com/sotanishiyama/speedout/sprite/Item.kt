package com.sotanishiyama.speedout.sprite

import com.sotanishiyama.speedout.GameView

class Item(gameView: GameView, x: Double, y: Double, deceleration: Int) : Sprite(gameView) {

    var speed: Double = 10 * gameView.scaleX
    var deceleration = Math.min(deceleration, 50)

    init {

        width = (40 * gameView.scaleX).toInt()
        height = width
        this.x = x - width / 2
        this.y = y - height / 2

    }
}
