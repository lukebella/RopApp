package com.apm.ropapp

import com.apm.ropapp.StatsSeason
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Stats : AppCompatActivity() {
    private lateinit var binding: StatsBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().reference.child("users")
                .child(userId).addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists())
                            binding.textView6.text =
                                dataSnapshot.child("username").getValue(String::class.java)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("ProfileFragment", "Failed to get user data.", databaseError.toException())
                    }
                })
        }

        binding.seasonButton.setOnClickListener {
            intent = Intent(this, StatsSeason::class.java)
            Log.d("Stats", "Season Stats")
            startActivity(intent)
        }

        binding.clothesButton.setOnClickListener {
            intent = Intent(this, StatsType::class.java)
            Log.d("Stats", "Clothes Stats")
            startActivity(intent)

        }
        binding.backButton.setOnClickListener {
            Log.d("Stats", "Back to MainActivity")
            finish()
        }

    }
}
