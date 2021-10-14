package com.brenbailey.wheredyouparkthecar.ui.maps


import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

class MapViewModel : ViewModel() {


    private val _carLocation = MutableLiveData<LatLng>()
    val carLocation: LiveData<LatLng>
        get()= _carLocation

    fun carLocationSetter() {
        _carLocation.value = _currentLocation.value?.let { LatLng(it.latitude, it.longitude) }
    }

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: MutableLiveData<Location?>
        get()= _currentLocation



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