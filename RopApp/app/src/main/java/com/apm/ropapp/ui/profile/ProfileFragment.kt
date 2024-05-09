package com.apm.ropapp.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apm.ropapp.EditProfile
import com.apm.ropapp.Login
import com.apm.ropapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        binding.configButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            Log.d("Profile", "Editing profile")
            startActivity(intent)
        }
        binding.infoButton.setOnClickListener {
            Log.d("Profile", "Info button clicked")
        }

        binding.statButton.setOnClickListener {
            Log.d("Profile", "Stats button clicked")
        }

        binding.logOutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()

        }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}