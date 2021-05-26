package com.decagon.android.sq007.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.decagon.android.sq007.view.MapsActivity

object LocationPermisson {

    // check for permission
    fun checkPermission(mapsActivity: MapsActivity): Boolean {
        return ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

}