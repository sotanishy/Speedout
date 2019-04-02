package com.sotanishiyama.speedout

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.app.FragmentActivity

class PauseButton(private val gameView: GameView, private val x: Int, private val y: Int, private val size: Int) : PauseDialogFragment.PauseDialogListener {

    val rect: Rect
        get() = Rect(x, y, x + size, y + size)

    private var fragment = PauseDialogFragment()

    var isDialogShowing = false

    fun draw(canvas: Canvas) {
        val paint = Paint()

        paint.color = Color.LTGRAY
        canvas.drawRect(
                (x + size / 5).toFloat(),
                (y + size / 5).toFloat(),
                (x + size * 2 / 5).toFloat(),
                (y + size * 4 / 5).toFloat(),
                paint
        )
        canvas.drawRect(
                (x + size * 3 / 5).toFloat(),
                (y + size / 5).toFloat(),
                (x + size * 4 / 5).toFloat(),
                (y + size * 4 / 5).toFloat(),
                paint
        )
    }

    override fun showDialog() {
        if (fragment.isAdded) return
        isDialogShowing = true
        fragment.isCancelable = false
        fragment.show((gameView.context as FragmentActivity).supportFragmentManager, "pause")
    }

    override fun resume() {
        isDialogShowing = false
        gameView.resume()
    }

    override fun restart() {
        isDialogShowing = false
        gameView.saveHighScore()
        gameView.restart()
        gameView.resume()
    }

    override fun goBackToMainMenu() {
        isDialogShowing = false
        (gameView.context as Activity).finish()
    }
}
