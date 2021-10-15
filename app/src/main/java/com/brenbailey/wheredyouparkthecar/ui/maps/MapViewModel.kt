package com.brenbailey.wheredyouparkthecar.ui.maps


import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.*

class MapViewModel : ViewModel() {



    private val _carLocation = MutableLiveData<Location>()
    val carLocation: LiveData<Location>
        get()= _carLocation

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: MutableLiveData<Location?>
        get()= _currentLocation

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double>
        get()= _distance

    fun carLocationSetter() {
        _carLocation.value = currentLocation.value
    }

    fun savedCarLocationGetter(savedCarLocation: Location?) {
        _carLocation.value = savedCarLocation!!
    }

    fun distanceCalculator() {
        if (currentLocation.value != null && carLocation.value != null) {
            val latDistance =
                Math.toRadians(currentLocation.value!!.latitude - carLocation.value!!.latitude)
            val longDistance =
                Math.toRadians(currentLocation.value!!.longitude - carLocation.value!!.longitude)
            val latitudeCar = Math.toRadians(carLocation.value!!.latitude)
            val latitudeCurrent = Math.toRadians((currentLocation.value!!.latitude))

            val a = sin(latDistance / 2).pow(2.0) +
                    sin(longDistance / 2).pow(2.0) *
                    cos(latitudeCar) *
                    cos(latitudeCurrent)

            val rad = 6371
            val c = 2 * asin(sqrt(a))
            _distance.value = rad * c
            Log.d("distance", _distance.value.toString())
        } else {
            Log.d("null check ", "failed")
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationClient: FusedLocationProviderClient) {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location == null) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { coarseLocation :  Location? ->
                            if (coarseLocation != null) {
                                _currentLocation.value = coarseLocation
                            } else {
                                _currentLocation.value = null
                            }
                            //unable to attain location
                        }
                } else {
                    _currentLocation.value = location
                }
            }
            .addOnCanceledListener {  }
    }

}