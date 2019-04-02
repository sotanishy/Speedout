package com.sotanishiyama.speedout.geom

import android.graphics.Rect

class Rectangle(left: Double, top: Double, right: Double, bottom: Double) {

    val left = Line(left, top, left, bottom)
    val top = Line(left, top, right, top)
    val right = Line(right, top, right, bottom)
    val bottom = Line(left, bottom, right, bottom)

    fun toRect(): Rect {
        return Rect(left.p.x.toInt(), top.p.y.toInt(), right.p.x.toInt(), bottom.p.y.toInt())
    }
}
