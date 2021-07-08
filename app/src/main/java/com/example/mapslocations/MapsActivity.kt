package com.example.mapslocations

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapslocations.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        requestAccessFineLocation()
        when{
isFineLocationGranted()->{
   // setupLocationListener()
    when{
        isLocationEnabled()->setupLocationListener()
        else->showGPSNotEnabled()
    }
}
            else->requestAccessFineLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
           999-> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //setupLocationListener() [for One time location code] below is GPS oNE
               when{
                   isLocationEnabled()->setupLocationListener()
                   else->showGPSNotEnabled()
               }
            }
                else{
                    Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun setupLocationListener(){
        //realtimeLOCATION using fusedLocationProviderClient
       val fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        val locationRequest=LocationRequest()
            .setInterval(2000)
            .setFastestInterval(2000)
            .setSmallestDisplacement(1f)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
        object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for(location in locationResult.locations ){
                    val current=LatLng(location.latitude,location.longitude)
                    if(::mMap.isInitialized){

                        mMap.addMarker(MarkerOptions().position(current).title("Marker in Sydney"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
                    }
                }
            }

        },
        Looper.myLooper()
            )
       /* //togetUsersCurrentLocation[But its not accurate and also only 1 time so we use fused location provider]
val lm=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers=locationManager.getProviders(true)

        var l:Location?=null
        for(i in providers.indices.reversed()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            l=locationManager.getLastKnownLocation(providers[i])
            if(l!=null) break
        }
        l.let{
            if(::mMap.isInitialized){
                val current = it?.let { it1 -> LatLng(it.latitude, it1.longitude) }
                mMap.addMarker(MarkerOptions().position(current).title("Marker in Sydney"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
            }
        }*/
    }



    fun isFineLocationGranted():Boolean{
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    }


    private fun requestAccessFineLocation() {
this.requestPermissions(
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
    999
)
    }

    //to check GPS enabled or not
    fun isLocationEnabled(): Boolean {
return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
//to show a dialog to enable GPS
    fun showGPSNotEnabled(){
        AlertDialog.Builder(this)
            .setTitle("Enable GPS ")
            .setMessage("Gps is REQUIRED")
            .setCancelable(false)
            .setPositiveButton("Enable Now"){
                dialogInterface: DialogInterface,i:Int->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialogInterface.dismiss()
            }.show()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //2 video
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled=true           //zoom
            isZoomGesturesEnabled=true           //use fingers for zoom
            isMyLocationButtonEnabled=true      //forButtons
            isCompassEnabled=true

        }
      //  mMap.setMapStyle()
        mMap.setMaxZoomPreference(2f) //maximum how much can zoom

        // Add a marker in Sydney and move the camera
        //1 video
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.addPolyline(


            PolylineOptions()
                .add(sydney,LatLng(20.59,78.39))
                .color(ContextCompat.getColor(baseContext,R.color.black))
        )
            .width=2f
    }
}