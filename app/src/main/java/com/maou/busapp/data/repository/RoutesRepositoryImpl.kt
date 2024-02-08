package com.maou.busapp.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.mapping.toListModel
import com.maou.busapp.data.source.response.GeneralResponse
import com.maou.busapp.data.source.response.RoutesResponse
import com.maou.busapp.data.source.service.GoogleMapsService
import com.maou.busapp.domain.model.Points
import com.maou.busapp.domain.model.Routes
import com.maou.busapp.domain.repository.RoutesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class RoutesRepositoryImpl(
    private val context: Context,
    private val googleMapsService: GoogleMapsService
) : RoutesRepository {
    override fun getAllRoutes(): Flow<BaseResult<List<Routes>, String>> {
        return flow {
            val response = getRoutes()
            if (response.statusCode != 200) {
                emit(BaseResult.Error("Error"))
            }
            emit(BaseResult.Success(response.body.toListModel()))
        }.catch { e ->
            emit(BaseResult.Error(e.message.toString()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getDirections(
        origin: LatLng,
        destination: LatLng
    ): Flow<BaseResult<List<LatLng>, String>> {
        return flow {
            val directions = googleMapsService.getDirections(origin, destination)
        }
    }

    private fun getRoutes(): GeneralResponse<List<RoutesResponse>> {
        lateinit var jsonString: String
        try {
            jsonString = context.assets.open("usk_bus_routes.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            Log.d("Error", ioException.message.toString())
        }
        val response = object : TypeToken<GeneralResponse<List<RoutesResponse>>>() {}.type
        return Gson().fromJson(jsonString, response)
    }
}