package com.waqt.app.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Handles GPS coordinate retrieval, reverse-geocoding, and background displacement registration.
 */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Obtains the immediate current GPS location.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                return@withContext location
            }

            // Fallback to last known location if getCurrentLocation returned null
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resolves human-readable locality/city name from latitude and longitude.
     */
    suspend fun getLocalityName(latitude: Double, longitude: Double): String =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext "Lat: %.2f, Lng: %.2f".format(latitude, longitude)

                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    val address = addresses.firstOrNull()
                                    continuation.resume(extractLocality(address, latitude, longitude))
                                }

                                override fun onError(errorMessage: String?) {
                                    continuation.resume("Lat: %.2f, Lng: %.2f".format(latitude, longitude))
                                }
                            }
                        )
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    extractLocality(address, latitude, longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Lat: %.2f, Lng: %.2f".format(latitude, longitude)
            }
        }

    private fun extractLocality(address: Address?, latitude: Double, longitude: Double): String {
        if (address == null) return "Lat: %.2f, Lng: %.2f".format(latitude, longitude)
        return address.locality
            ?: address.subAdminArea
            ?: address.adminArea
            ?: address.countryName
            ?: "Lat: %.2f, Lng: %.2f".format(latitude, longitude)
    }

    /**
     * Registers a PendingIntent with FusedLocationProviderClient to monitor 5 km displacement.
     */
    @SuppressLint("MissingPermission")
    fun startDisplacementTracking() {
        if (!hasLocationPermission()) return

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                15 * 60 * 1000L // Check every 15 minutes
            ).apply {
                setMinUpdateDistanceMeters(5000f) // 5 km displacement threshold
                setWaitForAccurateLocation(false)
            }.build()

            val pendingIntent = getLocationPendingIntent()
            fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopDisplacementTracking() {
        try {
            val pendingIntent = getLocationPendingIntent()
            fusedLocationClient.removeLocationUpdates(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocationPendingIntent(): PendingIntent {
        val intent = Intent(context, LocationUpdateReceiver::class.java).apply {
            action = LocationUpdateReceiver.ACTION_LOCATION_UPDATE
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, LOCATION_PENDING_INTENT_REQ_CODE, intent, flags)
    }

    companion object {
        private const val LOCATION_PENDING_INTENT_REQ_CODE = 9001
    }
}
