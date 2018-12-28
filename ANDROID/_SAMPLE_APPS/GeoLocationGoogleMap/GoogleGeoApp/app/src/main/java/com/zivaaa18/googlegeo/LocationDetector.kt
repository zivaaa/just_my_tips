package com.zivaaa18.googlegeo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle

/**
 * Official tutorial
 * https://developer.android.com/guide/topics/location/strategies
 *
 *
 */
class LocationDetector(
    private val ctx: Context,
    private var minUpdateTime: Long = 1000 * 5,
    private var minUpdateDistance: Float = 10f
) {
    companion object {
        val LOCATION_PERMISSION_CODE = 7771
    }

    private val BETTER_INTERVAL: Long = 1000 * 10

    /**
     * Interface of listener
     */
    interface OuterLocationListener {
        fun onLocationDetected(location: Location)
        fun onEachLocationUpdate(location: Location)
        fun onProviderStatusChanged(provider: String, status: Int)
        fun onProviderEnabled(provider: String)
        fun onProviderDisabled(provider: String)
    }

    class PermissionNotGrantedException : Exception()

    private var currentLocation: Location? = null

    private var listening = false

    private var outerListener: OuterLocationListener? = null

    private var locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var gpsProviderOn = true
    var networkProviderOn = true

    /**
     * Geo location listener
     */
    private var innerListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location == null) {
                return
            }

            outerListener?.onEachLocationUpdate(location)

            if (isBetterLocation(location, currentLocation)) {
                setCurrentLocation(location)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            if (provider == null) {
                return
            }

            outerListener?.onProviderStatusChanged(provider, status)
        }

        override fun onProviderEnabled(provider: String?) {
            if (provider == null) {
                return
            }

            outerListener?.onProviderEnabled(provider)
        }

        override fun onProviderDisabled(provider: String?) {
            if (provider == null) {
                return
            }

            outerListener?.onProviderDisabled(provider)
        }
    }

    /**
     * Update location and notify listener
     */
    private fun setCurrentLocation(loc: Location) {
        currentLocation = loc
        outerListener?.onLocationDetected(loc)
    }

    @SuppressWarnings("MissingPermission")
    @Throws(PermissionNotGrantedException::class)
    fun startListening() {
        if (doesHaveRequiredPermission()) {
            if (listening) {
                stopListening()
            }

            var networkLocation: Location? = null;
            var gpsLocation: Location? = null;

            if (gpsProviderOn) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minUpdateTime,
                    minUpdateDistance,
                    innerListener
                )
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }


            if (networkProviderOn) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minUpdateTime,
                    minUpdateDistance,
                    innerListener
                )
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }


            listening = true

            /**
             * Use best location from providers if found.
             */
            if (networkLocation != null && gpsLocation != null) {
                if (isBetterLocation(networkLocation, gpsLocation)) {
                    setCurrentLocation(networkLocation)
                } else {
                    setCurrentLocation(gpsLocation)
                }
            } else if (gpsLocation != null) {
                setCurrentLocation(gpsLocation)
            } else if (networkLocation != null) {
                setCurrentLocation(networkLocation)
            }
        } else {
            throw PermissionNotGrantedException()
        }
    }

    fun stopListening() {
        if (listening) {
            locationManager.removeUpdates(innerListener)
        }
        listening = false
    }

    fun requestPermission(activity: Activity) {
        if (android.os.Build.VERSION.SDK_INT > 22) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > BETTER_INTERVAL
        val isSignificantlyOlder: Boolean = timeDelta < -BETTER_INTERVAL

        when {
            isSignificantlyNewer -> return true
            isSignificantlyOlder -> return false
        }

        // Check whether the new location fix is more or less accurate
        val isNewer: Boolean = timeDelta > 0L
        val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
        val isLessAccurate: Boolean = accuracyDelta > 0f
        val isMoreAccurate: Boolean = accuracyDelta < 0f
        val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

        // Determine location quality using a combination of timeliness and accuracy
        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate -> true
            else -> false
        }
    }


    fun doesHaveRequiredPermission(): Boolean {
        return ctx.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionsValid(requestCode: Int): Boolean {
        if (requestCode != LOCATION_PERMISSION_CODE) {
            return false
        }

        return doesHaveRequiredPermission()
    }

    fun setOuterLocationListener(listener: OuterLocationListener) {
        this.outerListener = listener
    }

    fun removeOuterLocationListener() {
        this.outerListener = null
    }

}