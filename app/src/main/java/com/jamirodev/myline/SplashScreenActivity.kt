package com.jamirodev.myline

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val imageView: ImageView = findViewById(R.id.guruId)

        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 2000
        imageView.startAnimation(fadeIn)
        showWelcome()
    }

    fun showWelcome() {
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }
}