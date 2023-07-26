package com.appcloos.mimaletin.ModuloReten

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ActivityModuloRetenBinding

class ModuloRetenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuloRetenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuloRetenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        binding.toolBar.title = "Modulo Reten"
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentReten) as NavHostFragment
        navHostFragment.findNavController().run {
            binding.toolBar.setupWithNavController(this, AppBarConfiguration(graph))
        }

        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}