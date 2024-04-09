package com.apm.ropapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.apm.ropapp.databinding.LoginBinding
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {
    private lateinit var binding: LoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth= FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener() {
            Log.d("Login", "Se hizo clic en Iniciar sesión")

            val email = binding.userLogin.text.toString()
            val password = binding.userPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener()
                {
                    if(it.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Debe rellenar todos los campos", Toast.LENGTH_SHORT).show()
            }
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
