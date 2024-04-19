package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.LoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var binding: LoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference

        binding.loginButton.setOnClickListener {
            Log.d("Login", "Se hizo clic en Iniciar sesión")

            val email = binding.userLogin.text.toString()
            val password = binding.userPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) finish()
                    else Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            } else Toast.makeText(this, "Debe rellenar todos los campos", Toast.LENGTH_SHORT).show()
        }

        binding.signupButton.setOnClickListener {
            Log.d("Login", "Se hizo clic en el Crear Cuenta")
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.loginFacebookbutton.setOnClickListener {
            Log.d("Login", "Se hizo clic en Iniciar sesión con Facebook")

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
            Log.d("1", "pasa por aqui")

            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

                    override fun onSuccess(result: LoginResult) {
                        result.let {
                            val token = it.accessToken
                            val credential = FacebookAuthProvider.getCredential(token.token)
                            Log.d("2", "pasa por aqui")
                            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = firebaseAuth.currentUser
                                    Toast.makeText(this@Login, "Signed in as ",
                                        Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Log.d("Login", "Authentication failed")
                                    Toast.makeText(this@Login, "Authentication failed",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onCancel() {
                        Log.d("Login", "Authentication cancelled")
                        Toast.makeText(this@Login, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException) {
                        Log.d("Login", "Authentication failed")
                        Toast.makeText(this@Login, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                })

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.loginGooglebutton.setOnClickListener {
            Log.d("Login", "Se hizo clic en Iniciar sesión con Google")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("381231263922-i3ip3jbcdmn641h4uvde1gbrg7noue5m.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val userId = user?.uid.toString()

                val queryToGetData: Query =
                    database.child("users").child(userId).equalTo(user?.uid.toString())
                queryToGetData.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            //val intent = Intent(this@Login, CompleteData::class.java)
                            //startActivity(intent)
                            finish()

                        } else {
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
        }
    }

}

