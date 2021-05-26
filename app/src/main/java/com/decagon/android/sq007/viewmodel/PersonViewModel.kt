package com.decagon.android.sq007.viewmodel

//import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.android.sq007.NODE_MIKE
import com.decagon.android.sq007.model.Location
import com.google.android.gms.maps.model.LatLng
//import com.decagon.android.sq007.view.MapsActivity.Companion.Aniete
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PersonViewModel: ViewModel() {

    lateinit var ani: Location

    // get firebase reference
    var fireBaseRef = FirebaseDatabase.getInstance().reference

    // holds value to be observed by view model in main activity
    val Aniete: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>()
    }

    // save my location to firebase
    fun addContact(latLng: LatLng) {
        fireBaseRef.child(NODE_MIKE).setValue(latLng)
    }

    // responds to the value event listener from Maps Activity and takes a snapshot on data change
    val databaseListener = object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                // get data from a specific node in firebase
                ani = snapshot.child("Anietie").getValue(Location::class.java)!!

                // sends data to mutable livedata
                Aniete.value = ani
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    }
}
