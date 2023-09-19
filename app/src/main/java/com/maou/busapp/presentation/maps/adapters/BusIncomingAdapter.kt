package com.maou.busapp.presentation.maps.adapters

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maou.busapp.databinding.BusIncomingItemBinding
import com.maou.busapp.domain.model.BusIncoming
import com.maou.busapp.utils.convertTimeToStringMinute

@SuppressLint("NotifyDataSetChanged")
class BusIncomingAdapter: RecyclerView.Adapter<BusIncomingAdapter.BusIncomingViewHolder>() {

    private val busIncomingList = arrayListOf<BusIncoming>()
    private var onItemSelectedListener: ItemSelectedListener? = null

    fun setList(list: List<BusIncoming>) {
        busIncomingList.clear()
        busIncomingList.addAll(list)

        notifyDataSetChanged()
    }

    fun setItemSelected(itemSelected: (busIncoming: BusIncoming) -> Unit) {
        onItemSelectedListener = object : ItemSelectedListener {
            override fun onItemSelected(busIncoming: BusIncoming) {
                itemSelected(busIncoming)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusIncomingViewHolder {
        val binding = BusIncomingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusIncomingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return busIncomingList.size
    }

    override fun onBindViewHolder(holder: BusIncomingViewHolder, position: Int) {
        holder.bind(busIncomingList[position])
    }

    inner class BusIncomingViewHolder(
        private val binding: BusIncomingItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(busIncoming: BusIncoming) {
            with(binding) {
                val timeInMinute = convertTimeToStringMinute(busIncoming.etaToCurrStop)
                val info = if (timeInMinute != 0L) {
                    "<b>${busIncoming.busCarPlate}</b> arrive in <b>$timeInMinute minutes</b>"
                } else {
                    "<b>${busIncoming.busCarPlate}</b> <b>has arrived in the stop</b>"
                }

                tvBusName.text = Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY)

            }

            itemView.setOnClickListener {
                onItemSelectedListener?.onItemSelected(busIncoming)
                notifyDataSetChanged()
            }
        }
    }

    interface ItemSelectedListener {
        fun onItemSelected(busIncoming: BusIncoming)
    }
}