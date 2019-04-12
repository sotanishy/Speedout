package com.sotanishiyama.speedout

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element


class AboutPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val privacyPolicy = Element()
                .setTitle("Privacy Policy")
                .setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/speedoutprivacypolicy")))
                }

        val aboutPage = AboutPage(this)
                .setDescription("Classic arcade game with the accelerating ball. Can you keep up with the speed?")
                .setImage(R.drawable.feature_graphics)
                .addItem(Element().setTitle("Version " + packageManager.getPackageInfo(packageName, 0).versionName))
                .addItem(privacyPolicy)
                .addEmail("2480souta@gmail.com")
                .addPlayStore("com.sotanishiyama.speedout")
                .addGitHub("sotanishy/speedout")
                .create()

        setContentView(aboutPage)
    }
}
