package com.appcloos.mimaletin.moduloCXC

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appcloos.mimaletin.Constantes
import com.appcloos.mimaletin.colorAccentAgencia
import com.appcloos.mimaletin.colorToolBarAux
import com.appcloos.mimaletin.databinding.FragmentModuloCxcBinding
import com.appcloos.mimaletin.moduloCXC.fragments.EdoGenCuentaFragment
import com.appcloos.mimaletin.moduloCXC.fragments.PlanificadorCXCFragment

class ModuloCXCFragment : Fragment() {
    private lateinit var binding: FragmentModuloCxcBinding
    private lateinit var preferences: SharedPreferences
    private var codEmpresa: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentModuloCxcBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = requireContext().getSharedPreferences("Preferences", MODE_PRIVATE)
        codEmpresa = preferences.getString("codigoEmpresa", null)

        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(PlanificadorCXCFragment(), "Planificador")
        adapter.addFragment(EdoGenCuentaFragment(), "Edo. General Cuenta")
        binding.viewPager.adapter = adapter
        binding.tbLayout.apply {
            setupWithViewPager(binding.viewPager)
            setBackgroundColor(colorToolBarAux(Constantes.AGENCIA))
            setSelectedTabIndicatorColor(colorAccentAgencia(Constantes.AGENCIA))
        }
    }
}
