package com.brenbailey.wheredyouparkthecar.ui.maps

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.brenbailey.wheredyouparkthecar.R
import com.brenbailey.wheredyouparkthecar.databinding.MapFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private var mapReady = false
    private lateinit var carLocation: LatLng
    private var binding: MapFragmentBinding? = null

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }

    val callback = OnMapReadyCallback { googleMap ->
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("View?", " is going on?")
        val fragmentBinding = MapFragmentBinding.inflate(inflater)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.let {
            viewModel.carLocation.observe(viewLifecycleOwner, {
                    carLocation -> this.carLocation = carLocation
                updateMap()
            })

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragmentId) as? SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap
            mapReady = true
            //updateMap()
            val sydney = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

}

    private fun updateMap() {
        if (mapReady && carLocation != null) {
        }
    }
}