package com.brenbailey.wheredyouparkthecar.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel() {

    private val _carLocation = MutableLiveData<LatLng>()
    val carLocation: LiveData<LatLng> = _carLocation
    
    fun setCarLocation(locationOfCar: LatLng) {
        _carLocation.value = locationOfCar
    }

}