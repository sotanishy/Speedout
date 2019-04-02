package com.sotanishiyama.speedout

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var buttonPlay: Button? = null
    private var buttonHighScore: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        buttonPlay = findViewById(R.id.buttonPlay)
        buttonPlay!!.setOnClickListener(this)
        buttonHighScore = findViewById(R.id.buttonHighScore)
        buttonHighScore!!.setOnClickListener(this)

        MobileAds.initialize(this, resources.getString(R.string.app_id))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonPlay -> startActivity(Intent(this, GameActivity::class.java))

            R.id.buttonHighScore -> {
                val sharedPref = getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                var message = ""
                for (i in 1..5) {
                    val highScore = sharedPref.getString("${resources.getString(R.string.high_score_key)}_$i", "0,0").split(",")
                    message += "$i Level ${highScore[0]} Score ${highScore[1]}\n"
                }
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.high_score)
                        .setMessage(message)
                        .create()
                        .show()
            }
        }

    }
}
