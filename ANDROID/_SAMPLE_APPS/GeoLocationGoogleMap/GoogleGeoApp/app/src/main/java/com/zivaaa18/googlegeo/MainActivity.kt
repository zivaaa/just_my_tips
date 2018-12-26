package com.zivaaa18.googlegeo

import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val DEFAULT_ZOOM = 14f
    }

    private var currentZoomLevel = DEFAULT_ZOOM
    private var map: GoogleMap? = null
    private lateinit var detector: LocationDetector
    private var curLocation: Location? = null
    private var zoomUpdateConst = true
    private var moveOnUpdate = true
    private var markerMe: Marker? = null

    private var detectorListener = object : LocationDetector.OuterLocationListener {
        override fun onLocationDetected(location: Location) {
            curLocation = location
            updateMapByLocation(curLocation)
        }

        override fun onEachLocationUpdate(location: Location) {
            Toast.makeText(this@MainActivity, "updating request detected (${location.latitude},${location.longitude}) ${location.provider}", Toast.LENGTH_SHORT)
                .show()
        }

        override fun onProviderStatusChanged(provider: String, status: Int) {
            updateProviderStatus(
                provider, when (status) {
                    LocationProvider.AVAILABLE -> {
                        "AVAILABLE ${status}"
                    }
                    LocationProvider.OUT_OF_SERVICE -> {
                        "OUT_OF_SERVICE ${status}"
                    }
                    LocationProvider.TEMPORARILY_UNAVAILABLE -> {
                        "TEMPORARILY_UNAVAILABLE ${status}"
                    }
                    else -> {
                        "Unknown ${status}"
                    }
                }
            )
        }

        override fun onProviderEnabled(provider: String) {
            updateProviderStatus(provider, "ENABLED")
        }

        override fun onProviderDisabled(provider: String) {
            updateProviderStatus(provider, "DISABLED")
        }
    }

    private fun updateMapByLocation(loc : Location?) {
        if (loc == null) {
            return
        }

        val latLong = LatLng(loc.latitude, loc.longitude)

        if (moveOnUpdate) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLong,
                    if (zoomUpdateConst) DEFAULT_ZOOM else currentZoomLevel
                )
            )
        }

        markerMe?.position = latLong
        locationView.text = "alt: ${loc.latitude}, lng: ${loc.longitude} |  ${loc.provider}"
        additionalInfoView.text = "speed: ${loc.speed} \n accuracy: ${loc.accuracy}"
    }

    private fun updateProviderStatus(provider: String, status: String) {
        if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            statusNetwork.text = status
        } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
            statusGPS.text = status
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync {
            onMapReady(it)
        }

        detector = LocationDetector(this.applicationContext)

        detector.setOuterLocationListener(detectorListener)

        try {
            detector.startListening()
        } catch (e: LocationDetector.PermissionNotGrantedException) {
            detector.requestPermission(this)
        }

        networkSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                detector.networkProviderOn = isChecked
                detector.startListening()
                locationView.text = ""
            }
        })
        gpsSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                detector.gpsProviderOn = isChecked
                detector.startListening()
                locationView.text = ""
            }
        })

        constZoomSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                zoomUpdateConst = isChecked
            }
        })

        moveOnUpdateSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                moveOnUpdate = isChecked
            }
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                setMapMode(GoogleMap.MAP_TYPE_NONE)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        setMapMode(GoogleMap.MAP_TYPE_NORMAL)
                    }
                    1 -> {
                        setMapMode(GoogleMap.MAP_TYPE_HYBRID)
                    }
                    2 -> {
                        setMapMode(GoogleMap.MAP_TYPE_SATELLITE)
                    }
                    3 -> {
                        setMapMode(GoogleMap.MAP_TYPE_TERRAIN)
                    }
                    4 -> {
                        setMapMode(GoogleMap.MAP_TYPE_NONE)
                    }
                }
            }
        }
    }

    private fun setMapMode(mode : Int) {
        map?.mapType = mode
    }

    override fun onResume() {
        super.onResume()
        detector.setOuterLocationListener(detectorListener)
    }

    override fun onPause() {
        super.onPause()
        detector.removeOuterLocationListener()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LocationDetector.LOCATION_PERMISSION_CODE) {
            if (detector.checkPermissionsValid(requestCode)) {
                detector.startListening()
            }
        }
    }

    fun onMapReady(map: GoogleMap) {
        this.map = map
        map.setOnCameraMoveListener {
            currentZoomLevel = map.cameraPosition.zoom
        }
        markerMe = map.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)).title("Me"))
        updateMapByLocation(curLocation)
    }
}
