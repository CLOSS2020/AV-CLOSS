package com.appcloos.mimaletin.moduloCXC


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ActivityModuloCxcBinding


class ModuloCXCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuloCxcBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuloCxcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        binding.toolBar.title = "Modulo Cobranza"
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navHostFragment.findNavController().run {
            binding.toolBar.setupWithNavController(this, AppBarConfiguration(graph))
        }

        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }


        /*navController = findNavController(R.id.fragment)
        drawerLayout = findViewById(R.id.drawer_layout)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)*/
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/

}