package com.brenbailey.wheredyouparkthecar.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
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
import com.google.android.material.dialog.MaterialDialogs

class MapFragment : Fragment() {
    private val viewModel: MapViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var layout: View
    private lateinit var currentLocationMarker: Marker
    private lateinit var carLocationMarker: Marker
    private lateinit var mMap: GoogleMap
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    var permissionsGranted: Boolean = false
    private val requiredPermissionsList = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private var mapReady = false
    private var showDistanceDialog = false
    private var dialog: AlertDialog? = null


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
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
        setHasOptionsMenu(true)
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
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        layout = view

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragmentId) as? SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap
            mapReady = true
            currentLocationMarker = mMap.addMarker(
                MarkerOptions().position(LatLng(-33.3, 45.4)).title("User Location")
                    .visible(false)
            )
            carLocationMarker = mMap.addMarker(
                MarkerOptions().position(LatLng(-33.3, 45.4)).title("User Location")
                    .visible(false)
            )

            showDistanceDialog = sharedPreferences.getBoolean("showDistanceDialog", false)

            view.let {
                viewModel.carLocation.observe(viewLifecycleOwner, { carLocation ->
                    val location =
                        setOf(carLocation.latitude.toString(), carLocation.longitude.toString())
                    sharedPreferences?.edit()?.putStringSet("car_location", location)?.apply()
                    setCarLocation(carLocation)
                })

                viewModel.currentLocation.observe(viewLifecycleOwner, { currentLocation ->
                    if (currentLocation != null) {
                        updateMyLocation(currentLocation)
                    }
                })

                viewModel.distance.observe(viewLifecycleOwner, { currentDistance ->
                    if (showDistanceDialog) {
                        showDistanceDialog(currentDistance)
                    }
                })

                val savedCarLocation: Set<String>? =
                    sharedPreferences?.getStringSet("car_location", setOf(""))

                viewModel.savedCarLocationGetter(savedCarLocation)

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.findDistance -> {
                // navigate to settings screen
                Log.d("fragment menu click", " triggered")
                if (dialog?.isShowing == true) {
                    dialog!!.dismiss()
                }
                    viewModel.distanceCalculator()
                    showDistanceDialog = true
                    sharedPreferences.edit().putBoolean("showDistanceDialog", showDistanceDialog)
                        .apply()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onPause() {
        super.onPause()
    }


    private fun setCarLocation(carLocation: Location) {
        //val bitmap = Bitmap.createBitmap(R.drawable.ic_my_location)
        val latLng = LatLng(carLocation.latitude, carLocation.longitude)

        val icon: Bitmap = BitmapFactory.decodeResource(activity?.resources, R.drawable.ic_car_map)
        carLocationMarker.remove()
        carLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Car Location")
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_foreground)))
    }

    private fun updateMyLocation(currentLocation: Location) {

        val myLocLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        /*
        mMap.addCircle(CircleOptions().center(myLocLatLng).radius(1.0).fillColor(
            Color.BLUE))

         */
        currentLocationMarker.remove()

        currentLocationMarker = mMap.addMarker(
            MarkerOptions().position(myLocLatLng).title("User Location")
                .flat(true)
        )

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocLatLng, 12.0f))
    }

    fun showDistanceDialog(currentDistance: Double) {
        layout.showDialog(
            "Title",
            "You are " + currentDistance + " miles from your car",
            getString(R.string.ok)
        ) {
            showDistanceDialog = false
            sharedPreferences.edit().putBoolean("showDistanceDialog", showDistanceDialog).apply()
        }
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
            ) == PackageManager.PERMISSION_GRANTED &&
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
                    "Location Access",
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
        title: String,
        msg: String,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {

        if (actionMessage != null) {
            materialAlertDialogBuilder = MaterialAlertDialogBuilder(
                context
            )
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(actionMessage) { dialog, which ->
                    action(this)
                }

        } else {
            materialAlertDialogBuilder = MaterialAlertDialogBuilder(
                context, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
            )
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                }
        }
        materialAlertDialogBuilder.show().setCanceledOnTouchOutside(false)

    }

    private fun getLocation(fusedLocationCLient: FusedLocationProviderClient) {
        viewModel.getLocation(fusedLocationCLient)
    }
}