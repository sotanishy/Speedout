package com.sotanishiyama.speedout

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment

class PauseDialogFragment : DialogFragment() {

    interface PauseDialogListener {
        fun showDialog()
        fun resume()
        fun restart()
        fun goBackToMainMenu()
    }

    private var listener: PauseDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.pause_menu_title)
                .setItems(R.array.pause_array) { dialog, which ->
                    when (which) {
                        0 -> { // resume
                            listener?.resume()
                        }

                        1 -> { // restart
                            val confirmationDialog = AlertDialog.Builder(activity)
                            confirmationDialog
                                    .setMessage(resources.getString(R.string.restart_confirmation_message))
                                    .setCancelable(false)
                                    .setPositiveButton("Yes") { dialog, which ->
                                        listener?.restart()
                                    }
                                    .setNegativeButton("No") { dialog, which ->
                                        listener?.showDialog()
                                    }
                                    .create()
                            confirmationDialog.show()
                        }

                        2 -> { // main menu
                            val confirmationDialog = AlertDialog.Builder(activity)
                            confirmationDialog
                                    .setMessage(resources.getString(R.string.main_menu_confirmation_message))
                                    .setCancelable(false)
                                    .setPositiveButton("Yes") { dialog, which ->
                                        listener?.goBackToMainMenu()
                                    }
                                    .setNegativeButton("No") { dialog, which ->
                                        listener?.showDialog()
                                    }
                                    .create()
                            confirmationDialog.show()

                        }
                    }
                }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.listener = (context as GameActivity).gameView?.pauseButton
    }
}
