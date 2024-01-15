package com.maou.busapp.presentation.home

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.maou.busapp.databinding.BusStopItemBinding
import com.maou.busapp.domain.model.BusStop
import java.util.Locale

class BusStopAdapter : RecyclerView.Adapter<BusStopAdapter.BusStopViewHolder>() {

    private val busStopList = arrayListOf<BusStop>()
    private var onItemSelectedListener: ItemSelectedListener? = null

    fun setItemSelected(onItemSelected: (busStop: BusStop) -> Unit) {
        onItemSelectedListener = object : ItemSelectedListener {
            override fun onItemSelected(busStop: BusStop) {
                onItemSelected(busStop)
            }
        }
    }

    fun setList(list: List<BusStop>) {
        busStopList.clear()
        busStopList.addAll(list)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusStopViewHolder {
        val viewBinding =
            BusStopItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusStopViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: BusStopViewHolder, position: Int) {
        holder.bind(busStopList[position])
    }

    override fun getItemCount(): Int = busStopList.size
    inner class BusStopViewHolder(
        private val binding: BusStopItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(busStop: BusStop) {
            with(binding) {
                tvBusStopNumber.text = busStop.busStopName
                tvBusStopLocation.text = getBusStopLocation(
                    LatLng(
                        busStop.latitude.toDouble(),
                        busStop.longitude.toDouble()
                    )
                )

            }

            itemView.setOnClickListener {
                onItemSelectedListener?.onItemSelected(busStop)
                notifyDataSetChanged()
            }
        }

        private fun getBusStopLocation(location: LatLng): String? {
            val geocoder = Geocoder(itemView.context, Locale.getDefault())
            var address: Address? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    address = addresses[0]
                }
            } else {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                address = addresses!![0]
            }

            return address?.getAddressLine(0)
        }
    }

    interface ItemSelectedListener {
        fun onItemSelected(busStop: BusStop)
    }

}