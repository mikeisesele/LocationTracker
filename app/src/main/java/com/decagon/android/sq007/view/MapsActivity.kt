package com.decagon.android.sq007.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.decagon.android.sq007.viewmodel.PersonViewModel
import com.decagon.android.sq007.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.decagon.android.sq007.databinding.ActivityMapsBinding
import com.decagon.android.sq007.model.Location
import com.decagon.android.sq007.util.LocationPermisson
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.FirebaseDatabase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    // initialize global variables
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var REQUEST_CODE: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fireBaseRef = FirebaseDatabase.getInstance().reference
    lateinit var viewModel: PersonViewModel

    // variable to hold live data coming from view model
    var AnieteLocation: Location = Location()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initialize fused Location Client to hep in getting location
        fusedLocationClient =  LocationServices.getFusedLocationProviderClient(this)

        // initialize view model
        viewModel = ViewModelProvider(this).get(PersonViewModel::class.java)

        // set observer on mutable list go get current data
        viewModel.Aniete.observe(this, {
            AnieteLocation = it
        })


        // listen to database for changes update the view model on any data change
        fireBaseRef.addValueEventListener(viewModel.databaseListener)

    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        getLocationUpdates()
    }
    
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // check for permission
        getLocationPermission()
    }

    private fun getLocationPermission(){
        if(LocationPermisson.checkPermission(this@MapsActivity)){
            // if permission is granted, get the latest updates
            getLocationUpdates()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            mMap.isMyLocationEnabled = true
        }   else{
            ActivityCompat.requestPermissions(
                this,arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // if permission is granted after being denied at first, get update
        if(grantResults.contains(PackageManager.PERMISSION_GRANTED) && requestCode == REQUEST_CODE){
            getLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission is required for this feature to run", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // function to get latest update
    private fun getLocationUpdates(){
        // set intervals
        locationRequest = LocationRequest()
        locationRequest.interval = 3000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {

            // when call for latest location is ready, commence map update below
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {

                    val location = locationResult.lastLocation

                    // set last location to entities
                    val MikeLatLng = LatLng(location.latitude, location.longitude)
                    val AniLatLng = LatLng(AnieteLocation.latitude, AnieteLocation.Longitude)

                    // update database of the latest location
                    viewModel.addContact(MikeLatLng)

                    // create markers
                    val mike = MarkerOptions().position(MikeLatLng).title("Michael")
                    val ani = MarkerOptions().position(AniLatLng).title("Ani").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    //        mMap.addMarker(MarkerOptions().position(sydney).title("Michael").icon(BitmapDescriptorFactory.fromResource(R.Drawable.----name)))

                    // clear map for previous markers
                    mMap.clear()

                    // add new marker on each location update
                    mMap.addMarker(mike)
                    mMap.addMarker(ani)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MikeLatLng, 18f))

                } else {
                    Log.d("else", "Error")
                }
            }
        }
    }
}



