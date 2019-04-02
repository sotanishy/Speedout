package com.sotanishiyama.speedout.geom

class Point(var x: Double, var y: Double) {

    fun distanceTo(p: Point): Double {
        val dx = p.x - this.x
        val dy = p.y - this.y
        return Math.sqrt(dx * dx + dy * dy)
    }

    fun isOn(l: Line): Boolean {
        val a = l.q.y - l.p.y
        val b = l.q.x - l.p.x
        val d = Math.abs(a * (x - l.p.x) - b * (y - l.p.y)) / Math.sqrt(a * a + b * b)
        return d < 1
    }
}
