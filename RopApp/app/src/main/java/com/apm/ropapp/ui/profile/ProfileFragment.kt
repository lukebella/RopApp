package com.apm.ropapp.ui.profile

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apm.ropapp.EditProfile
import com.apm.ropapp.Login
import com.apm.ropapp.Stats
import com.apm.ropapp.UserInfo
import com.apm.ropapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().reference.child("users").child(userId)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            binding.textView6.text =
                                dataSnapshot.child("username").getValue(String::class.java)
                            val imageUrl = dataSnapshot.child("image").getValue(String::class.java)
                            if (!imageUrl.isNullOrEmpty()) {
                                val imageRef =
                                    FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener { bytes ->
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    binding.ProfileImage.setImageBitmap(bitmap)
                                }.addOnFailureListener {
                                    // Handle any errors
                                    Toast.makeText(
                                        requireContext(),
                                        "Image upload failed: ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d(
                            "ProfileFragment",
                            "Failed to get user data.",
                            databaseError.toException()
                        )
                    }
                })
        }

        binding.configButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            Log.d("Profile", "Editing profile")
            startActivity(intent)
        }
        binding.infoButton.setOnClickListener {
            val intent = Intent(requireContext(), UserInfo::class.java)
            Log.d("Profile", "Info button clicked")
            startActivity(intent)
        }

        binding.statsButton.setOnClickListener {
            val intent = Intent(requireContext(), Stats::class.java)
            Log.d("Profile", "Stats button clicked")
            startActivity(intent)
        }

        binding.logOutButton.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle("Logout")
                .setMessage("Are you sure you want to logout?").setPositiveButton("Yes") { _, _ ->

                    //re-initialize SharedPreferences
                    requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
                        .edit().clear().apply()
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                }.setNegativeButton("Cancel", null).show()

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}