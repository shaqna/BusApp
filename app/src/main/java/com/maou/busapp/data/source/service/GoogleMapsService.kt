package com.maou.busapp.data.source.service

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TransitMode
import com.google.maps.model.TravelMode

class GoogleMapsService(private val geoApiContext: GeoApiContext) {

    suspend fun getDirections(origin: LatLng, destination: LatLng): List<LatLng>? {
        try {
            val request = DirectionsApi.getDirections(
                geoApiContext,
                "${origin.latitude},${origin.longitude}",
                "${destination.latitude},${destination.longitude}"
            ).mode(TravelMode.DRIVING).transitMode(TransitMode.BUS)

            val directionResult: DirectionsResult = request.await()

            if (directionResult.routes.isNotEmpty()) {
                val route = directionResult.routes[0].overviewPolyline.decodePath()

                route.map { LatLng(it.lat, it.lng) }
            }
        } catch (e: Exception) {
            Log.d("Directions API", e.message.toString())
            return null
        }


        return TODO("Provide the return value")
    }
}