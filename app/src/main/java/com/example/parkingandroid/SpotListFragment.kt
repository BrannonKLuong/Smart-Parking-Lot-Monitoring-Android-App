package com.example.parkingandroid
import com.example.parkingandroid.model.ParkingSpotData
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingandroid.adapter.SpotsAdapter
import com.example.parkingandroid.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpotListFragment : Fragment(R.layout.fragment_spot_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = SpotsAdapter()
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            try {
                // Added null check and safe call operator ?.
                val dtoList = ApiClient.service.getSpots().body()?.spots ?: emptyList()
                val spots = dtoList.map { dto ->
                    ParkingSpotData(id = dto.id, isFree = dto.is_available)
                }
                adapter.submitList(spots)
            } catch (e: Exception) {
                Log.e("SpotListFragment", "Failed to load spots", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load parking spots: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
