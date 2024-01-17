package com.appcloos.mimaletin.moduloCXC

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.appcloos.mimaletin.AdminSQLiteOpenHelper
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.ObjetoAux
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.colorToolBarAux
import com.appcloos.mimaletin.databinding.ActivityModuloCxcBinding
import com.appcloos.mimaletin.setThemeNoBarCXCAgencia
import com.appcloos.mimaletin.windowsColor

class ModuloCXCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuloCxcBinding
    private var codEmpresa: String? = null
    lateinit var conn: AdminSQLiteOpenHelper
    private lateinit var enlaceEmpresa: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuloCxcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        val codUsuario = preferences.getString("cod_usuario", null)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        enlaceEmpresa = conn.getCampoStringCamposVarios(
            "ke_enlace",
            "kee_url",
            listOf("kee_codigo"),
            listOf(codEmpresa!!)
        )

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
        objetoAux.descargaDesactivo(codUsuario!!, codEmpresa!!, enlaceEmpresa)

        // Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        // Edicion de los colores del Bar de arriba de notificacion de las app y el bar de abajo de los 3 botones
        windowsColor(Constantes.AGENCIA)
        setColors()
    }

    private fun setColors() {
        binding.toolBar.apply {
            setBackgroundColor(colorToolBarAux(Constantes.AGENCIA))
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(setThemeNoBarCXCAgencia(Constantes.AGENCIA), true)
        // you could also use a switch if you have many themes that could apply
        return theme
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/
}
