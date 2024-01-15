package com.maou.busapp.presentation.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maou.busapp.databinding.ActivityWelcomeBinding
import com.maou.busapp.presentation.home.HomeActivity
import com.maou.busapp.presentation.maps.MapsActivity

class WelcomeActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setButtonAction()
    }

    private fun setButtonAction() {
        binding.findBus.setOnClickListener {
            startActivity(Intent(this@WelcomeActivity, HomeActivity::class.java))
        }
    }


}