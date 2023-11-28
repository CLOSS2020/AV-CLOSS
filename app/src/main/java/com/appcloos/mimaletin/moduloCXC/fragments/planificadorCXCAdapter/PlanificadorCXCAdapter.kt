package com.appcloos.mimaletin.moduloCXC.fragments.planificadorCXCAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcloos.mimaletin.R
import com.appcloos.mimaletin.moduloCXC.viewmodel.PlanificadorCxc

class PlanificadorCXCAdapter(
    private var planificadorCxc: List<PlanificadorCxc> = emptyList(),
    private val onClickListener: (String, String) -> Unit,
    private var DIAS_VALIDOS_BOLIVARES: Int
) : RecyclerView.Adapter<PlanificadorCXCHolder>() {

    fun updateAdapter(newList: List<PlanificadorCxc>) {
        planificadorCxc = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanificadorCXCHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlanificadorCXCHolder(
            layoutInflater.inflate(
                R.layout.item_planificador_cxc, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PlanificadorCXCHolder, position: Int) {
        holder.render(planificadorCxc[position], onClickListener)
    }

    override fun getItemCount(): Int = planificadorCxc.size


}