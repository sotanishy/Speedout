package com.sotanishiyama.speedout

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class GameActivity : FragmentActivity() {

    private lateinit var ad: InterstitialAd

    var gameView: GameView? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        gameView = GameView(this)
        setContentView(gameView)

        ad = InterstitialAd(this)
        ad.adUnitId = resources.getString(R.string.ad_unit_id)
        // test id ca-app-pub-3940256099942544/1033173712
        ad.loadAd(AdRequest.Builder().addTestDevice("A9848A1D4E9C15DE78455F7EEBE1E281").build())
    }

    override fun onPause() {
        super.onPause()
        gameView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView!!.resume()
    }

    override fun onBackPressed() {
        gameView!!.pause()
        val confirmationDialog = AlertDialog.Builder(this)
        confirmationDialog
                .setTitle(resources.getString(R.string.main_menu_confirmation_message))
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                    gameView!!.resume()
                }
                .create()
        confirmationDialog.show()
    }

    fun showAd() {
        runOnUiThread {
            ad.show()
        }
    }
}
