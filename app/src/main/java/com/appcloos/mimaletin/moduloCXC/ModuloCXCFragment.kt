package com.appcloos.mimaletin.moduloCXC

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appcloos.mimaletin.databinding.FragmentModuloCxcBinding
import com.appcloos.mimaletin.moduloCXC.fragments.EdoGenCuentaFragment
import com.appcloos.mimaletin.moduloCXC.fragments.PlanificadorCXCFragment


class ModuloCXCFragment : Fragment() {
    private lateinit var binding: FragmentModuloCxcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentModuloCxcBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(PlanificadorCXCFragment(), "Planificador")
        adapter.addFragment(EdoGenCuentaFragment(), "Edo. General Cuenta")
        binding.viewPager.adapter = adapter
        binding.tbLayout.setupWithViewPager(binding.viewPager)
    }
}