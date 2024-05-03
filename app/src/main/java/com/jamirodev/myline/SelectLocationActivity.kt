package com.jamirodev.myline

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.jamirodev.myline.databinding.ActivitySelectLocationBinding

class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectLocationBinding

    private companion object {
        private const val DEFAULT_ZOOM = 15
    }

    private var myMap: GoogleMap? = null
    private var myPlaceClient: PlacesClient? = null
    private var myFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var myLastKnowLocation: Location? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var direction = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.listoLL.visibility = View.GONE

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.MapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(this, getString(R.string.my_google_maps_api_key))

        myPlaceClient = Places.createClient(this)
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val autocompleteSupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        val placeList = arrayOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        autocompleteSupportMapFragment.setPlaceFields(listOf(*placeList))

        autocompleteSupportMapFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val id = place.id
                val name = place.name
                val latlng = place.latLng

                selectedLatitude = latlng?.latitude
                selectedLongitude = latlng?.longitude
                direction = place.address ?: ""

                addMark(latlng, name, direction)
            }

            override fun onError(p0: Status) {
                Toast.makeText(applicationContext, "este es un error123", Toast.LENGTH_LONG).show()
            }
        })
        binding.IbGps.setOnClickListener {
            if (isGpsActivated()) {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                Toast.makeText(this, "Por favor active ubicación", Toast.LENGTH_LONG).show()
            }
        }

        binding.BtnListo.setOnClickListener {
            val intent = Intent()
            intent.putExtra("latitud", selectedLatitude)
            intent.putExtra("longitud", selectedLongitude)
            intent.putExtra("direction", direction)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun chooseActualPlace() {
        if (myMap == null) {
            return
        }
        detectAndShowDeviceLocationMap()
    }

    @SuppressLint("MissingPermission")
    private fun detectAndShowDeviceLocationMap() {
        try {
            val locationResult = myFusedLocationProviderClient!!.lastLocation
            locationResult.addOnSuccessListener { location ->
                if (location != null) {
                    myLastKnowLocation = location

                    selectedLatitude = location.latitude
                    selectedLongitude = location.longitude

                    val latLng = LatLng(selectedLatitude!!, selectedLongitude!!)
                    myMap!!.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            DEFAULT_ZOOM.toFloat()
                        )
                    )
                    myMap!!.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))

                    directionLatLng(latLng)
                }
            }
                .addOnFailureListener { e ->

                }
        } catch (e: Exception) {

        }
    }

    private fun isGpsActivated(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnable = false
        var networkEnable = false
        try {
            gpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {

        }
        try {
            networkEnable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {

        }
        return !(!gpsEnable && !networkEnable)
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                myMap!!.isMyLocationEnabled = true
                chooseActualPlace()
            } else {
                Toast.makeText(this, "Permiso rechazado", Toast.LENGTH_LONG).show()
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        myMap!!.setOnMapClickListener { latlng ->
            selectedLatitude = latlng.latitude
            selectedLongitude = latlng.longitude

            directionLatLng(latlng)
        }
    }

    private fun directionLatLng(latlng: LatLng) {
        val geoCoder = Geocoder(this)
        try {
            val addressList = geoCoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
            val address = addressList!![0]
            val addressLine = address.getAddressLine(0)
            val subLocality = address.subLocality
            direction = "$addressLine"
            addMark(latlng, "$subLocality", "$addressLine")
        } catch (e: Exception) {

        }
    }

    private fun addMark(latlng: LatLng, title: String, direction: String) {
        myMap!!.clear()
        try {
            val markerOptions = MarkerOptions()
            markerOptions.position(latlng)
            markerOptions.title("$title")
            markerOptions.snippet("$direction")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) //green icon

            myMap!!.addMarker(markerOptions)
            myMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM.toFloat()))

            binding.listoLL.visibility = View.VISIBLE
            binding.lugarSelectedTv.text = direction

        } catch (e: Exception) {

        }
    }
}

