package com.lazyshoppers.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.lazyshoppers.databinding.ActivitySplashBinding
import com.lazyshoppers.firestore.FirestoreClass

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUserID = FirestoreClass().getCurrentUserId()

            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}