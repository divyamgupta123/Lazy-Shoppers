package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lazyshoppers.databinding.ActivityMainBinding
import com.lazyshoppers.utils.Constants

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences(
            Constants.LS_PREFERENCE,
            Context.MODE_PRIVATE
        )
        val userName = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME,"")!!
        binding.tvName.text = "Hello $userName"

    }
}