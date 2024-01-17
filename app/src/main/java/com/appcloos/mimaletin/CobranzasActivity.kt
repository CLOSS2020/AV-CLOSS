package com.appcloos.mimaletin

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener

class CobranzasActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    var pagerAdapter: PagerController? = null
    var reciboCobranza = ReciboCobranza()
    var verRecibos = VerRecibos()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobranzas)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // mantener la orientacion vertical
        tabLayout = findViewById(R.id.TabLayout)
        viewPager = findViewById(R.id.ViewPager)

        val conn = AdminSQLiteOpenHelper(applicationContext, "ke_android", null)

        val preferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        val codUsuario = preferences.getString("cod_usuario", null)
        val codEmpresa = preferences.getString("codigoEmpresa", null)
        val enlaceEmpresa = conn.getCampoStringCamposVarios(
            "ke_enlace",
            "kee_url",
            listOf("kee_codigo"),
            listOf(codEmpresa!!)
        )
        pagerAdapter = PagerController(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = pagerAdapter
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                if (tab.position == 0) {
                    pagerAdapter!!.notifyDataSetChanged()
                }
                if (tab.position == 1) {
                    pagerAdapter!!.notifyDataSetChanged()
                }
                if (tab.position == 2) {
                    pagerAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        val objetoAux = ObjetoAux(this)
        objetoAux.descargaDesactivo(codUsuario!!, codEmpresa, enlaceEmpresa)
    }
}
