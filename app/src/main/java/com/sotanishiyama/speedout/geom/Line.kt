package com.sotanishiyama.speedout.geom

class Line(px: Double, py: Double, qx: Double, qy: Double) {

    var p: Point = Point(px, py)
    var q: Point = Point(qx, qy)

    companion object {

        // Given three collinear points p, q, r,
        // checks if point q lies on line segment pr
        private fun onSegment(p: Point, q: Point, r: Point): Boolean {
            // need this delta because when the line segment pr are horizontal or vertical
            // and the point q is off by a bit, this method won't say q is on the line segment
            val delta = .01
            return Math.min(p.x, r.x) - delta <= q.x && q.x <= Math.max(p.x, r.x) + delta &&
                    Math.min(p.y, r.y) - delta <= q.y && q.y <= Math.max(p.y, r.y) + delta
        }

        // returns the point of intersection of the two lines
        // returns null if two lines do not intersect
        // if two lines are collinear, returns the point closest to l.p
        // so l should always be the path of the ball
        fun getIntersectionPoint(l: Line, m: Line): Point? {
            var x = 0.0
            var y = 0.0
            // if perpendicular to x-axis
            if (l.p.x == l.q.x && m.p.x == m.q.x) {
                if (l.p.x == m.p.x) {
                    x = l.p.x

                    // if m.p is closer to l.p than m.q
                    y = if (l.p.distanceTo(m.p) < l.p.distanceTo(m.q)) {
                        m.p.y
                    } else {
                        m.q.y
                    }
                }
            } else if (l.p.x == l.q.x) {
                x = l.p.x
                y = (m.q.y - m.p.y) / (m.q.x - m.p.x) * (x - m.p.x) + m.p.y
            } else if (m.p.x == m.q.x) {
                x = m.p.x
                y = (l.q.y - l.p.y) / (l.q.x - l.p.x) * (x - l.p.x) + l.p.y
            } else {
                val slopeL = (l.q.y - l.p.y) / (l.q.x - l.p.x)
                val slopeM = (m.q.y - m.p.y) / (m.q.x - m.p.x)
                val interceptL = l.p.y - slopeL * l.p.x
                val interceptM = m.p.y - slopeM * m.p.x

                // if collinear
                if (slopeL == slopeM) {
                    if (interceptL == interceptM) {
                        // if m.p is closer to l.p than m.q
                        if (l.p.distanceTo(m.p) < l.p.distanceTo(m.q)) {
                            x = m.p.x
                            y = m.p.y
                        } else {
                            x = m.q.x
                            y = m.q.y
                        }
                    }
                } else {
                    x = -(interceptL - interceptM) / (slopeL - slopeM)
                    y = slopeL * x + interceptL
                }
            }

            val point = Point(x, y)
            return if (onSegment(l.p, point, l.q) && onSegment(m.p, point, m.q)) {
                point
            } else null
        }

        // returns the point of intersection of a line and a rectangle
        // returns null if they do not intersect
        // if they do intersect, returns the point closest to l.p
        fun getIntersectionPoint(l: Line, r: Rectangle): Point? {
            val points = arrayOfNulls<Point>(4)
            points[0] = Line.getIntersectionPoint(l, r.left)
            points[1] = Line.getIntersectionPoint(l, r.top)
            points[2] = Line.getIntersectionPoint(l, r.right)
            points[3] = Line.getIntersectionPoint(l, r.bottom)

            var minDistance = Double.MAX_VALUE
            var closestPoint: Point? = null
            for (p in points) {
                if (p != null) {
                    val distance = l.p.distanceTo(p)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestPoint = p
                    }
                }
            }
            return closestPoint
        }
    }
}
