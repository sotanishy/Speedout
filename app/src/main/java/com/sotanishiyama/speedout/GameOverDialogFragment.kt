package com.sotanishiyama.speedout

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment

class GameOverDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val sharedPref = activity!!.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        var message = ""
        for (i in 1..5) {
            val highScore = sharedPref.getString("${resources.getString(R.string.high_score_key)}_$i", "0,0").split(",")
            message += "$i Level ${highScore[0]} Score ${highScore[1]}\n"
        }

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.gameover_menu_title)
                .setMessage(message)
                .setPositiveButton(R.string.main_menu) { dialog, which ->
                    activity!!.finish()
                }
        return builder.create()
    }
}
