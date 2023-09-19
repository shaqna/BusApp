package com.maou.busapp.presentation.maps.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maou.busapp.databinding.BusEtaItemBinding
import com.maou.busapp.domain.model.BusEta
import com.maou.busapp.domain.model.BusIncoming
import com.maou.busapp.utils.StringUtils

@SuppressLint("NotifyDataSetChanged")
class BusEtaAdapter(
    private val onBusIncomingSelected: (BusIncoming) -> Unit
) : RecyclerView.Adapter<BusEtaAdapter.BusViewHolder>() {
    private val busEtaList = arrayListOf<BusEta>()

    fun setList(list: List<BusEta>) {
        busEtaList.clear()
        busEtaList.addAll(list)

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusEtaAdapter.BusViewHolder {
        val binding = BusEtaItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusEtaAdapter.BusViewHolder, position: Int) {
        holder.initBusIncomingAdapter()
        holder.bind(busEtaList[position])
    }

    override fun getItemCount(): Int = busEtaList.size


    inner class BusViewHolder(private val binding: BusEtaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val busIncomingAdapter: BusIncomingAdapter by lazy {
            BusIncomingAdapter().apply {
                setItemSelected {
                    onBusIncomingSelected(it)
                }
            }
        }

        fun initBusIncomingAdapter() {
            binding.rvBusIncoming.apply {
                adapter = busIncomingAdapter
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        fun bind(busEta: BusEta) {
            with(binding) {
                tvRouteName.text = StringUtils.generateText("Bus Routes: ${busEta.line}")
                busIncomingAdapter.setList(busEta.incoming)
            }
        }
    }


}