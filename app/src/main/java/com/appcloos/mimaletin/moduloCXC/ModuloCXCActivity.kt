package com.appcloos.mimaletin.moduloCXC


import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.appcloos.mimaletin.ObjetoAux
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.databinding.ActivityModuloCxcBinding
import java.security.AccessController.getContext





class ModuloCXCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuloCxcBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuloCxcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        val codUsuario = preferences.getString("cod_usuario", null)

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

        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(codUsuario!!)

        //Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        //Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        val window = this.window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val nightModeFlags: Int = this.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO){
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }else{
            window.statusBarColor = ContextCompat.getColor(this, R.color.blackColor1)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.blackColor2)
        }



    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/

}