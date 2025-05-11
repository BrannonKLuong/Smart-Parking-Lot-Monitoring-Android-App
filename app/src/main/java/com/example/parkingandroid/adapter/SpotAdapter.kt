package com.example.parkingandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter // Import ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingandroid.R
import com.example.parkingandroid.databinding.ItemSpotBinding
import com.example.parkingandroid.model.ParkingSpotData // Ensure this import is correct

// Change to extend ListAdapter
class SpotsAdapter : ListAdapter<ParkingSpotData, SpotsAdapter.SpotVH>(SpotDiffCallback()) {

    // DiffUtil callback to efficiently calculate list updates
    class SpotDiffCallback : DiffUtil.ItemCallback<ParkingSpotData>() {
        override fun areItemsTheSame(oldItem: ParkingSpotData, newItem: ParkingSpotData): Boolean {
            // Items are the same if their unique ID is the same
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ParkingSpotData, newItem: ParkingSpotData): Boolean {
            // Contents are the same if all relevant data fields are the same
            return oldItem == newItem // Data classes automatically implement equals() and hashCode()
        }
    }

    // The submitList function is now provided by ListAdapter, remove your manual one

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotVH {
        val binding = ItemSpotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SpotVH(binding)
    }

    override fun onBindViewHolder(holder: SpotVH, position: Int) {
        // Use getItem() provided by ListAdapter
        val spot = getItem(position)
        holder.bind(spot)
    }

    // getItemCount() is also provided by ListAdapter, remove your manual one
    // override fun getItemCount() = currentList.size // You can use currentList.size if needed

    inner class SpotVH(private val b: ItemSpotBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(data: ParkingSpotData) {
            b.spotId.text = "Spot ${data.id}"
            b.statusIndicator.setImageResource(
                if (data.isFree) R.drawable.ic_free
                else R.drawable.ic_occupied
            )
            // Optionally, set up click listeners:
            // b.root.setOnClickListener { /* handle click */ }
        }
    }
}
