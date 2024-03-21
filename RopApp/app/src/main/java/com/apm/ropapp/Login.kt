package com.apm.ropapp

import SignUp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.apm.ropapp.databinding.LoginBinding


class Login : AppCompatActivity() {
    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener() {
            Log.d("Login", "Se hizo clic en Iniciar sesión")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.signupButton.setOnClickListener() {
            Log.d("Login", "Se hizo clic en el Crear Cuenta")
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.loginFacebookbutton.setOnClickListener() {
            Log.d("Login", "Se hizo clic en Iniciar sesión con Facebook")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.loginXbutton.setOnClickListener() {
            Log.d("Login", "Se hizo clic en Iniciar sesión con X")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.loginGooglebutton.setOnClickListener() {
            Log.d("Login", "Se hizo clic en Iniciar sesión con Google")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}
