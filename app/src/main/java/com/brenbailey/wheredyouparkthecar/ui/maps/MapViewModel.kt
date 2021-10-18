package com.brenbailey.wheredyouparkthecar.ui.maps


import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brenbailey.wheredyouparkthecar.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.math.round

const val START_LOADING = 0
const val STOP_LOADING = 8
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

    private val _loading = MutableLiveData<Int>()
    val loading: LiveData<Int>
        get()= _loading

    private val _materialAlertDialogBuilder = MutableLiveData<MaterialAlertDialogBuilder>()
    val materialAlertDialogBuilder: LiveData<MaterialAlertDialogBuilder>
        get()= _materialAlertDialogBuilder

    fun carLocationSetter() {
        _carLocation.value = currentLocation.value
    }

    fun savedCarLocationGetter(savedCarLocation: Set<String>?) {
        if (!savedCarLocation?.elementAt(0).isNullOrEmpty()) {
            val savedLocation = Location(LocationManager.GPS_PROVIDER)
            if (savedCarLocation != null) {
                savedLocation.latitude = savedCarLocation.elementAt(0).toDouble()
                savedLocation.longitude = savedCarLocation.elementAt(1).toDouble()
                Log.d("saved lat", savedLocation.latitude.toString())
            }
            _carLocation.value = savedLocation
        }
    }

    fun distanceCalculator() {
        _loading.value = START_LOADING
        viewModelScope.launch(Dispatchers.Default) {
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

                val rad = 3958.756
                val c = 2 * asin(sqrt(a))
                viewModelScope.launch(Dispatchers.Main) {
                    _distance.value = round((rad * c) * 100) / 100
                    _loading.value = STOP_LOADING
                }

            } else {
                Log.d("null check ", "failed")
                _loading.value = STOP_LOADING

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationClient: FusedLocationProviderClient) {
        _loading.value = START_LOADING
        viewModelScope.launch(Dispatchers.IO) {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
            )
                .addOnSuccessListener { location: Location? ->
                    viewModelScope.launch(Dispatchers.Main) {
                        // Got last known location. In some rare situations this can be null.
                        if (location == null) {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { coarseLocation: Location? ->
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
                        _loading.value = STOP_LOADING
                    }
                }
                .addOnCanceledListener { }
        }
    }

}