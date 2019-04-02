package com.sotanishiyama.speedout

import android.content.Context
import android.graphics.*
import android.support.v4.app.FragmentActivity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

import com.sotanishiyama.speedout.geom.Line
import com.sotanishiyama.speedout.sprite.Ball
import com.sotanishiyama.speedout.sprite.Brick
import com.sotanishiyama.speedout.sprite.Item
import com.sotanishiyama.speedout.sprite.Paddle
import java.util.*

class GameView(context: Context) : SurfaceView(context), Runnable {

    @Volatile
    internal var playing: Boolean = false
    private var gameThread: Thread? = null

    val screenWidth: Int = context.resources.displayMetrics.widthPixels
    val screenHeight: Int = context.resources.displayMetrics.heightPixels
    val scaleX: Double = screenWidth / 720.0
    val scaleY: Double = screenHeight / 1280.0

    private val paddle: Paddle = Paddle(this)
    private val ball: Ball = Ball(this)
    private val bricks: Array<Brick>
    private val items = mutableListOf<Item>()

    private var lives: Int
    private var score: Int
    private var level: Int

    private val statusBarHeight = 75 * scaleY

    private var showBonus: Boolean = false

    val pauseButton: PauseButton = PauseButton(this, screenWidth - 100, 0, statusBarHeight.toInt())

    private var backgroundImg: Bitmap? = null

    private val paint: Paint = Paint()
    private val surfaceHolder: SurfaceHolder = holder


    init {

        val sharedPref = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        lives = sharedPref.getInt(resources.getString(R.string.saved_lives_key), resources.getInteger(R.integer.default_lives))
        score = sharedPref.getInt(resources.getString(R.string.saved_score_key), 0)
        level = sharedPref.getInt(resources.getString(R.string.saved_level_key), 1)

        val id = resources.getIdentifier("background", "drawable", context.packageName)
        backgroundImg = BitmapFactory.decodeResource(resources, id)
        backgroundImg = Bitmap.createScaledBitmap(backgroundImg, screenWidth, screenHeight, false)

        bricks = Array(Brick.NUMS) { i ->
            val r = i / Brick.COLUMNS
            val c = i % Brick.COLUMNS
            val color = when (c) {
                0 -> Brick.RED
                1 -> Brick.ORANGE
                2 -> Brick.YELLOW
                3 -> Brick.GREEN
                4 -> Brick.BLUE
                5 -> Brick.PURPLE
                else -> 0
            }
            Brick(this, r, c, color)
        }

        initGame()
    }

    private fun initGame() {
        ball.reset()
        items.clear()

        for (brick in bricks) {
            brick.isDestroyed = false
        }
    }

    override fun run() {
        while (playing) {
            draw()
            update()
            control()
        }
    }


    private fun update() {
        if (!ball.isMoving) {
            ball.x = paddle.centerX - ball.width / 2
            ball.y = paddle.y - ball.height
        } else {
            showBonus = false

            moveBall(ball.speed)

            val iter = items.iterator()

            while (iter.hasNext()) {
                val item = iter.next()
                item.y += item.speed
                // hit the paddle
                if (Rect.intersects(paddle.rectangle.toRect(), item.rectangle.toRect())) {
                    iter.remove()
                    ball.increaseSpeed(-item.deceleration)
                }
                // hit the bottom
                if (item.y > screenHeight) {
                    iter.remove()
                }
            }

            if (bricks.all { it.isDestroyed }) {
                level++
                if (level % 5 == 0) { // add a bonus ball every 5 level
                    showBonus = true
                    lives++
                    (context as GameActivity).showAd()
                }
                saveProgress()
                initGame()
            }
        }
    }

    private fun moveBall(distance: Double) {
        if (distance == 0.0) return

        // move the ball and get the line of path of four corners of the ball
        val upperLeft = Line(ball.x, ball.y, ball.x + ball.dx * distance, ball.y + ball.dy * distance)
        val upperRight = Line(ball.x + ball.width, ball.y, ball.x + ball.width + ball.dx * distance, ball.y + ball.dy * distance)
        val lowerLeft = Line(ball.x, ball.y + ball.height, ball.x + ball.dx * distance, ball.y + ball.height + ball.dy * distance)
        val lowerRight = Line(ball.x + ball.width, ball.y + ball.height, ball.x + ball.width + ball.dx * distance, ball.y + ball.height + ball.dy * distance)
        val paths = listOf(upperLeft, upperRight, lowerLeft, lowerRight)

        // check all the objects (paddle, walls, bricks) and get the closest collision point
        var closestCollision = upperLeft.q
        var targetPath = upperLeft
        var minDistance = distance
        var target = "NO COLLISION"

        // check paddle
        for (path in paths) {
            val point = Line.getIntersectionPoint(path, paddle.rectangle)
            point?.let {
                val d = path.p.distanceTo(point)
                if (d < minDistance && ball.dy > 0) {
                    target = "PADDLE"
                    minDistance = d
                    closestCollision = point
                    targetPath = path
                }
            }
        }

        // check walls
        val left = Line(0.0, 0.0, 0.0, (screenHeight + ball.height).toDouble())
        val top = Line(0.0, statusBarHeight, screenWidth.toDouble(), statusBarHeight)
        val right = Line(screenWidth.toDouble(), 0.0, screenWidth.toDouble(), (screenHeight + ball.height).toDouble())
        val bottom = Line(0.0, (screenHeight + ball.height).toDouble(), screenWidth.toDouble(), (screenHeight + ball.height).toDouble())
        val walls = listOf(left, top, right, bottom)

        for (path in paths) {
            for (wall in walls) {
                val point = Line.getIntersectionPoint(path, wall)
                point?.let {
                    val d = path.p.distanceTo(point)
                    if (d < minDistance) {
                        target = "WALL_" + if (wall == left && ball.dx < 0)
                            "LEFT"
                        else if (wall == top && ball.dy < 0)
                            "TOP"
                        else if (wall == right && ball.dx > 0)
                            "RIGHT"
                        else if (wall == bottom && ball.dy > 0)
                            "BOTTOM"
                        else return@let
                        minDistance = d
                        closestCollision = point
                        targetPath = path
                    }
                }
            }
        }

        // check bricks
        for (path in paths) {
            for (i in bricks.indices) {
                if (bricks[i].isDestroyed) continue
                val r = bricks[i].rectangle
                val point = Line.getIntersectionPoint(path, r)
                point?.let {
                    val d = path.p.distanceTo(point)
                    if (d < minDistance) {
                        target = "BRICK_${i}_" + if (point.isOn(r.left) && ball.dx > 0)
                            "LEFT"
                        else if (point.isOn(r.top) && ball.dy > 0)
                            "TOP"
                        else if (point.isOn(r.right) && ball.dx < 0)
                            "RIGHT"
                        else if (point.isOn(r.bottom) && ball.dy < 0)
                            "BOTTOM"
                        else return@let
                        minDistance = d
                        closestCollision = point
                        targetPath = path
                    } else if (d == minDistance && target.startsWith("BRICK")) {
                        target += "_${i}_" + if (point.isOn(r.left) && ball.dx > 0)
                            "LEFT"
                        else if (point.isOn(r.top) && ball.dy > 0)
                            "TOP"
                        else if (point.isOn(r.right) && ball.dx < 0)
                            "RIGHT"
                        else if (point.isOn(r.bottom) && ball.dy < 0)
                            "BOTTOM"
                        else return@let
                    }
                }
            }
        }

        // adjust the point of the collision so that the upper left corner moves to that point
        when (targetPath) {
            upperRight -> closestCollision.x -= ball.width
            lowerLeft -> closestCollision.y -= ball.height
            lowerRight -> {
                closestCollision.x -= ball.width
                closestCollision.y -= ball.height
            }
        }

        // move the ball to the closest collision point
        ball.x = closestCollision.x
        ball.y = closestCollision.y

        // take action according to what it collided against
        when {
            target == "PADDLE" -> {
                ball.increaseSpeed(level) // increase the speed by (level)%
                ball.setDirection(
                        ball.centerX - paddle.centerX,
                        (-paddle.width / 2).toDouble()
                )
            }

            target.startsWith("WALL") -> {
                val location = target.substring(5)
                when (location) {
                    "LEFT", "RIGHT" -> ball.setDirection(-ball.dx, ball.dy)

                    "TOP" -> ball.setDirection(ball.dx, -ball.dy)

                    "BOTTOM" -> {
                        ball.reset()
                        items.clear()
                        lives--
                        if (lives == 0) {
                            gameOver()
                            return
                        }
                    }
                }
            }

            target.startsWith("BRICK") -> {
                val info = target.substring(6).split("_")
                val numBricks = info.size / 2
                var xRef = false
                var yRef = false
                for (i in 0 until numBricks) {
                    when (info[2 * i + 1]) {
                        "LEFT", "RIGHT" -> xRef = true

                        "TOP", "BOTTOM" -> yRef = true
                    }
                    val index = info[2 * i].toInt()
                    if (!bricks[index].isDestroyed) {
                        bricks[index].isDestroyed = true
                        score += 100

                        if (Random().nextInt(100) < Math.min(level, 33)) { // min(level, 33) % chance of decreasing speed
                            items.add(Item(this, bricks[index].centerX, bricks[index].centerY, level))
                        }

                    }
                }
                if (xRef) {
                    ball.setDirection(-ball.dx, ball.dy)
                } else if (yRef) {
                    ball.setDirection(ball.dx, -ball.dy)
                }
            }
        }

        moveBall(distance - minDistance)
    }

    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            val canvas = surfaceHolder.lockCanvas()

            // draw background
            canvas.drawBitmap(backgroundImg, 0f, 0f, paint)

            // draw status bar
            paint.color = Color.LTGRAY
            canvas.drawLine(0f, statusBarHeight.toFloat(), screenWidth.toFloat(), statusBarHeight.toFloat(), paint)

            // draw the paddle
            paint.color = paddle.color
            canvas.drawRoundRect(
                    (paddle.x + 2).toFloat(),
                    (paddle.y + 2).toFloat(),
                    (paddle.x + paddle.width - 2).toFloat(),
                    (paddle.y + paddle.height - 2).toFloat(),
                    (paddle.y / 2).toFloat(),
                    (paddle.y / 2).toFloat(),
                    paint
            )

            // draw the ball
            paint.color = ball.color
            canvas.drawCircle(
                    ball.centerX.toFloat(),
                    ball.centerY.toFloat(),
                    (ball.width / 2).toFloat(),
                    paint
            )

            // draw bricks
            for (brick in bricks) {
                if (brick.isDestroyed) continue
                paint.color = brick.color
                canvas.drawRect(
                        (brick.x + 2).toFloat(),
                        (brick.y + 2).toFloat(),
                        (brick.x + brick.width - 2).toFloat(),
                        (brick.y + brick.height - 2).toFloat(),
                        paint
                )
            }

            // draw items
            for (item in items) {
                paint.shader = LinearGradient(
                        0f,
                        0f,
                        (50 * scaleX).toFloat(),
                        (50 * scaleY).toFloat(),
                        Brick.BLUE,
                        Brick.GREEN,
                        Shader.TileMode.MIRROR
                )
                canvas.drawCircle(
                        item.centerX.toFloat(),
                        item.centerY.toFloat(),
                        (item.width / 2).toFloat(),
                        paint
                )
                paint.shader = null
                paint.color = Color.LTGRAY
                paint.textSize = (25 * scaleX).toFloat()
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("-${item.deceleration}%", item.centerX.toFloat(), (item.y - 10).toFloat(), paint)
            }

            // display lives
            paint.color = ball.color
            canvas.drawCircle(
                    (30 * scaleX).toFloat(),
                    (statusBarHeight / 2).toFloat(),
                    (16 * scaleX).toFloat(),
                    paint
            )

            paint.color = Color.LTGRAY
            paint.textSize = (30 * scaleX).toFloat()
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("x $lives", (55 * scaleX).toFloat(), (50 * scaleY).toFloat(), paint)

            // display the score
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("$score", (screenWidth * 2 / 7).toFloat(), (50 * scaleY).toFloat(), paint)

            // display the level
            paint.textSize = (40 * scaleX).toFloat()
            canvas.drawText("$level%", (screenWidth / 2).toFloat(), (50 * scaleY).toFloat(), paint)

            // display the speed
            paint.textSize = (30 * scaleX).toFloat()
            val speed = if (ball.isMoving) ball.speed else 0.0
            canvas.drawText("${"%.2f".format(speed)} m/s", (screenWidth * 5 / 7).toFloat(), (50 * scaleY).toFloat(), paint)

            // pause button
             pauseButton.draw(canvas)

            // display the level at the beginning
            if (!ball.isMoving) {
                paint.color = Color.WHITE
                paint.textSize = (300 * scaleX).toFloat()
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("$level%", (screenWidth * .5).toFloat(), (screenHeight * .6).toFloat(), paint)

                if (showBonus) {
                    paint.textSize = (50 * scaleX).toFloat()
                    canvas.drawText("1 Bonus Ball Awarded", (screenWidth * .5).toFloat(), (screenHeight * .7).toFloat(), paint)
                }
            }

            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        try {
            Thread.sleep(17)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        pauseButton.showDialog()
    }

    fun resume() {
        if (!pauseButton.isDialogShowing) {
            playing = true
            gameThread = Thread(this)
            gameThread!!.start()
        } else {
            // When the user temporarily exits the app and comes back, the surface does not become valid in this method.
            // Running draw() on a different thread would draw the game screen behind the dialog
            Thread(Runnable {
                Thread.sleep(50)
                draw()
            }).start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!playing) return true

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> if (pauseButton.rect.contains(event.x.toInt(), event.y.toInt())) {
                pause()
            }

            MotionEvent.ACTION_UP -> if (!ball.isMoving) {
                ball.isMoving = true
            }

            MotionEvent.ACTION_MOVE -> paddle.setCenterX(event.x.toDouble())
        }
        return true
    }

    private fun gameOver() {
        playing = false
        saveHighScore()
        val fragment = GameOverDialogFragment()
        fragment.isCancelable = false
        fragment.show((context as FragmentActivity).supportFragmentManager, "gameover")
        restart()
    }

    fun saveHighScore() {
        val sharedPref = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val highScores = Array<String>(5) {
            sharedPref.getString("${resources.getString(R.string.high_score_key)}_${it + 1}", "0,0")
        }

        if (highScores.last().split(",")[1].toInt() < score) {
            highScores[highScores.lastIndex] = "$level,$score"
        }

        highScores.sortWith(compareBy { it.split(",")[1].toInt() })
        highScores.reverse()

        with (sharedPref.edit()) {
            for (i in highScores.indices) {
                putString("${resources.getString(R.string.high_score_key)}_${i + 1}", highScores[i])
            }
            commit()
        }

    }

    private fun saveProgress() {
        val sharedPref = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt(resources.getString(R.string.saved_lives_key), lives)
            putInt(resources.getString(R.string.saved_score_key), score)
            putInt(resources.getString(R.string.saved_level_key), level)
            commit()
        }
    }

    fun restart() {
        lives = resources.getInteger(R.integer.default_lives)
        score = 0
        level = 1
        showBonus = false
        saveProgress()
        initGame()
        paddle.setCenterX(screenWidth / 2.0)
    }
}
