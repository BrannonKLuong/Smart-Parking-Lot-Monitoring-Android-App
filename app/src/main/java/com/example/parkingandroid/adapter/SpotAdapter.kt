package com.example.parkingandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingandroid.R
import com.example.parkingandroid.databinding.ItemSpotBinding
import com.example.parkingandroid.model.ParkingSpotData

class SpotsAdapter : ListAdapter<ParkingSpotData, SpotsAdapter.SpotVH>(SpotDiffCallback()) {

    // DiffUtil callback to efficiently calculate list updates
    class SpotDiffCallback : DiffUtil.ItemCallback<ParkingSpotData>() {
        override fun areItemsTheSame(oldItem: ParkingSpotData, newItem: ParkingSpotData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ParkingSpotData, newItem: ParkingSpotData): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotVH {
        val binding = ItemSpotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SpotVH(binding)
    }

    override fun onBindViewHolder(holder: SpotVH, position: Int) {

        val spot = getItem(position)
        holder.bind(spot)
    }

    inner class SpotVH(private val b: ItemSpotBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(data: ParkingSpotData) {
            b.spotId.text = "Spot ${data.id}"
            b.statusIndicator.setImageResource(
                if (data.isFree) R.drawable.ic_free
                else R.drawable.ic_occupied
            )
        }
    }
}
