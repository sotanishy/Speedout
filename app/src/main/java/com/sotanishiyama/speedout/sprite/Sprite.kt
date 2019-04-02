package com.sotanishiyama.speedout.sprite

import com.sotanishiyama.speedout.GameView

import com.sotanishiyama.speedout.geom.Rectangle

open class Sprite(protected val gameView: GameView) {

    var x: Double = 0.0
    var y: Double = 0.0
    var width: Int = 0
        protected set
    var height: Int = 0
        protected set

    var color: Int = 0
        protected set

    val centerX: Double
        get() = x + width / 2

    val centerY: Double
        get() = y + width / 2

    val rectangle: Rectangle
        get() = Rectangle(x, y, x + width, y + height)

}
