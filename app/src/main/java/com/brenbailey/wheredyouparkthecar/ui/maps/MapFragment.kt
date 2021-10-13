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

    private lateinit var carLocation: LatLng
    private lateinit var mMap: GoogleMap
    private lateinit var layout: View
    private var binding: MapFragmentBinding? = null
    private val requiredPermissionsList = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    private var mapReady = false
    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        permissions.entries.forEach{
            Log.e("DEBUG", "${it.key} = ${it.value}")
        }
    }

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

    /*
    - Google Documentation as of 10.13.21
    If your app targets Android 12 or higher, you can't request the ACCESS_FINE_LOCATION permission
    by itself. You must also request the ACCESS_COARSE_LOCATION permission, and you must include
    both permissions in a single runtime request. If you try to request only ACCESS_FINE_LOCATION,
    the system ignores the request
     */

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
                //Continue action that requires permission
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
                requestMultiplePermissions.launch(
                    requiredPermissionsList
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