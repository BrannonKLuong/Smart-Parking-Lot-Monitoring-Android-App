package com.example.parkingandroid.adapter
import com.example.parkingandroid.model.ParkingSpotData
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingandroid.R
import com.example.parkingandroid.databinding.ItemSpotBinding

class SpotsAdapter : RecyclerView.Adapter<SpotsAdapter.SpotVH>() {
    private val items = mutableListOf<ParkingSpotData>()

    fun submitList(newList: List<ParkingSpotData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotVH {
        val binding = ItemSpotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SpotVH(binding)
    }

    override fun onBindViewHolder(holder: SpotVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

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
