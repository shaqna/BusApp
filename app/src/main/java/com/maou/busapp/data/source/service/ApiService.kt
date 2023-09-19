package com.maou.busapp.data.source.service

import com.maou.busapp.data.source.request.ActiveBusStopRequest
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.BusStopRequest
import com.maou.busapp.data.source.request.GeneralRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.data.source.response.ActiveBusStopResponse
import com.maou.busapp.data.source.response.BusEtaResponse
import com.maou.busapp.data.source.response.BusLocationResponse
import com.maou.busapp.data.source.response.BusStopResponse
import com.maou.busapp.data.source.response.GeneralResponse
import com.maou.busapp.data.source.response.PredictionBusPathResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/default/app/busstoplist")
    suspend fun getBusStopList(
        @Body request: GeneralRequest<BusStopListRequest>
    ): GeneralResponse<List<BusStopResponse>>

    @GET("/default/app/getbuslocation")
    suspend fun getBusLocationInfo(): GeneralResponse<List<BusLocationResponse>>

    @POST("/default/app/getbuseta")
    suspend fun getBusEtaInfo(
        @Body request: GeneralRequest<BusEtaRequest>
    ): GeneralResponse<List<BusEtaResponse>>

    @POST("/default/app/activebusstoplist")
    suspend fun getActiveBusStop(
        @Body request: GeneralRequest<ActiveBusStopRequest>
    ): GeneralResponse<ActiveBusStopResponse>

    @POST("default/app/getbusstopinfo")
    suspend fun getBusStopInfo(
        @Body request: GeneralRequest<BusStopRequest>
    ): GeneralResponse<BusStopResponse>

    @POST("default/app/getpredictionbuspath")
    suspend fun getPredictionBusPath(
        @Body request: GeneralRequest<PredictionBusPathRequest>
    ): GeneralResponse<PredictionBusPathResponse>
}