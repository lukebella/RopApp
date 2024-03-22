package com.apm.ropapp

import CustomDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.apm.ropapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
//        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.floatingAddButton.setOnClickListener {
            Log.d("Main Activity", "Se hizo clic en el botón flotante")
            showCustomDialog()
            Log.d("TAG", navController.currentDestination?.id.toString())
            Log.d("TAG",R.id.navigation_closet.toString())
            //onDestinationchangedListener
        }
    }


    private fun showCustomDialog() {
        // Crear e instanciar el Custom Dialog
        val customDialog = CustomDialog(this)
        // Mostrar el Custom Dialog
        customDialog.show()
    }
}