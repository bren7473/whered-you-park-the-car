package com.brenbailey.wheredyouparkthecar.ui.maps

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.brenbailey.wheredyouparkthecar.R
import com.brenbailey.wheredyouparkthecar.databinding.MapFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MapFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var layout: View
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

        layout = view
        view.let {
            viewModel.carLocation.observe(viewLifecycleOwner, { carLocation ->
                this.carLocation = carLocation
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

                checkPermissions()
            }
        }

    }

    private fun updateMap() {
        TODO("Not yet implemented")
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED  &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                layout.showDialog(
                    getString(R.string.permission_granted),
                    null
                ) {}
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                layout.showDialog(
                    getString(R.string.permission_required),
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    fun View.showDialog(
        msg: String,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {

        if (actionMessage != null) {
            MaterialAlertDialogBuilder(context,
                R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setMessage(msg)
                .setPositiveButton(actionMessage) { dialog, which ->
                    action(this)
                }
                .show()
        } else {
            MaterialAlertDialogBuilder(context,
                R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setMessage(msg)
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                    // Respond to positive button press
                }
                .show()
        }
    }

}