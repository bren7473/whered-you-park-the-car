package com.brenbailey.wheredyouparkthecar.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.brenbailey.wheredyouparkthecar.R
import com.brenbailey.wheredyouparkthecar.databinding.MapFragmentBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MapFragment : Fragment() {
    private val viewModel: MapViewModel by viewModels()

    private lateinit var carLocation: LatLng
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var layout: View
    private lateinit var currentLocationMarker: Marker
    private lateinit var carLocationMarker: Marker
    private lateinit var mMap: GoogleMap

    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    var permissionsGranted: Boolean = false
    private val requiredPermissionsList = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    private var mapReady = false

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        permissions.entries.forEach{
            Log.e("DEBUG", "${it.key} = ${it.value}")
            permissionsGranted = it.value
        }
        if (permissionsGranted == true) {
            getLocation(fusedLocationClient)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.map_fragment, container, false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        layout = view
        view.let {
            viewModel.carLocation.observe(viewLifecycleOwner, { carLocation ->
                this.carLocation = carLocation
                setCarLocation()
            })

            viewModel.currentLocation.observe(viewLifecycleOwner, { currentLocation ->
                if (currentLocation != null) {
                    this.currentLocation = currentLocation
                    updateMyLocation()
                }
            })

            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapFragmentId) as? SupportMapFragment?
            mapFragment?.getMapAsync { googleMap ->
                mMap = googleMap
                mapReady = true
                currentLocationMarker = mMap.addMarker(MarkerOptions().position(LatLng(-33.3, 45.4)).title("User Location")
                    .visible(false))
                carLocationMarker = mMap.addMarker(MarkerOptions().position(LatLng(-33.3, 45.4)).title("User Location")
                    .visible(false))

                checkPermissions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onPause() {
        super.onPause()
    }




    private fun setCarLocation() {
        carLocationMarker.remove()
        carLocationMarker = mMap.addMarker(MarkerOptions().position(carLocation).title("Car Location"))
    }

    private fun updateMyLocation() {

        val myLocLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        /*
        mMap.addCircle(CircleOptions().center(myLocLatLng).radius(1.0).fillColor(
            Color.BLUE))

         */
        currentLocationMarker.remove()
        currentLocationMarker = mMap.addMarker(MarkerOptions().position(myLocLatLng).title("User Location")
            .alpha(.55F)
            .flat(true))

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocLatLng, 12.0f))
    }
    

    /*
    - Google Documentation as of 10.13.21
    If your app targets Android 12 or higher, you can't request the ACCESS_FINE_LOCATION permission
    by itself. You must also request the ACCESS_COARSE_LOCATION permission, and you must include
    both permissions in a single runtime request. If you try to request only ACCESS_FINE_LOCATION,
    the system ignores the request
     */

    @SuppressLint("MissingPermission")
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
                getLocation(fusedLocationClient)
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                layout.showDialog(
                    getString(R.string.permission_required),
                    getString(R.string.ok)
                ) {
                    requestMultiplePermissions.launch(
                        requiredPermissionsList
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

    private fun getLocation(fusedLocationCLient: FusedLocationProviderClient) {
        viewModel.getLocation(fusedLocationCLient)
    }

}