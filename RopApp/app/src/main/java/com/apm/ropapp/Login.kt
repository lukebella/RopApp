package com.apm.ropapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.apm.ropapp.databinding.LoginBinding


class Login : AppCompatActivity() {
    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupButton.setOnClickListener() {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}
