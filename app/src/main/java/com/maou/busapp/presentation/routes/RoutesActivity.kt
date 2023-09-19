package com.maou.busapp.presentation.routes

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TransitMode
import com.google.maps.model.TravelMode
import com.maou.busapp.R
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.databinding.ActivityRoutesBinding
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.presentation.maps.MapsActivity
import com.maou.busapp.presentation.state.BusStopUiState
import com.maou.busapp.presentation.state.PredictionBusPathUiState
import com.maou.busapp.presentation.state.RoutesUiState
import com.maou.busapp.utils.Constants
import com.maou.busapp.utils.iconToBitmap
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class RoutesActivity : AppCompatActivity() {
    private var routePolyline: Polyline? = null
    private lateinit var mMap: GoogleMap
    private val busStopMarkers: MutableList<Marker> = mutableListOf()
    private val viewModel: RoutesViewModel by viewModel()
    private var busMarker: Marker? = null

    private val binding: ActivityRoutesBinding by lazy {
        ActivityRoutesBinding.inflate(layoutInflater)
    }

    private val geoApiContext: GeoApiContext by lazy {
        GeoApiContext.Builder().apiKey(getString(R.string.api_key)).build()
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val updateBusLocation = object : Runnable {
        override fun run() {
            val request = PredictionBusPathRequest("F0:7F:7B:44:1E:7A")
            viewModel.fetchPredictionBusPath(request)

            handler.postDelayed(this, 5000L)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadKoinModules(RoutesViewModel.inject())

        busMarker?.remove()

        setButton()
        initMap()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapRoutes) as SupportMapFragment

//        val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            intent.getParcelableExtra(NAME, Location::class.java)
//        } else {
//            intent.getParcelableExtra(NAME)
//        }

        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.usmLocation, 14f))



            fetchBusStop()
            fetchRoutes()
            fetchPredictionBusPath()
            observeBusStop()
            observeRoutes()
            observePredictionBusPath()

        }
    }

    private fun observerStates() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.predictionBusPathUiState.collect {
                        handlePredictionBusPath(it)
                    }
                }
                launch {
                    viewModel.busStopState.collect { state ->
                        handleBusStopState(state)
                    }
                }
                launch {
                    viewModel.routesState.collect {
                        handleRoutes(it)
                    }
                }
            }
        }
    }

    private fun observePredictionBusPath() {
        viewModel.predictionBusPathUiState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                handlePredictionBusPath(it)
            }
            .launchIn(lifecycleScope)
    }

    private fun handlePredictionBusPath(state: PredictionBusPathUiState) {
        when (state) {
            is PredictionBusPathUiState.Error -> {
                Log.d("ShaqTagRoutes", state.message)
            }

            PredictionBusPathUiState.Init -> Unit
            is PredictionBusPathUiState.Loading -> {}
            is PredictionBusPathUiState.Success -> {
                val busPath = LatLng(state.data.latitude, state.data.longitude)
                busMarker = mMap.addMarker(
                    MarkerOptions().title("Bus").position(busPath)
                        .icon(iconToBitmap(this@RoutesActivity, R.drawable.icon_bus))
                )
            }
        }
    }

    private fun fetchPredictionBusPath() {
        handler.postDelayed(updateBusLocation, 5000L)
    }

    private fun observeBusStop() {
        viewModel.busStopState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleBusStopState(state)
            }
            .launchIn(lifecycleScope)
    }

    private fun handleBusStopState(state: BusStopUiState) {
        when (state) {
            is BusStopUiState.Error -> Log.d(MapsActivity.TAG, state.message)
            BusStopUiState.Init -> Unit
            is BusStopUiState.Loading -> {}
            is BusStopUiState.Success -> {
                showBusStopLocation(state.data)
            }
        }

    }

    private fun showBusStopLocation(data: List<BusStop>) {
        data.forEach { busStop: BusStop ->
            val latLng = LatLng(busStop.latitude.toDouble(), busStop.longitude.toDouble())
            val marker = mMap.addMarker(
                MarkerOptions().title(busStop.busStopName).position(latLng)
                    .icon(iconToBitmap(this@RoutesActivity, R.drawable.ic_bus_stop))
            )

            marker?.let {
                busStopMarkers.add(it)
            }

        }
    }

    private fun fetchBusStop() {

        val request =
            BusStopListRequest(
                country = Constants.Country.MY,
                state = Constants.State.PULAU_PINANG
            )
        viewModel.fetchBusStops(request)
    }

    private fun observeRoutes() {
        viewModel.routesState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                handleRoutes(it)
            }.launchIn(lifecycleScope)
    }

    private fun handleRoutes(state: RoutesUiState) {
        when (state) {
            is RoutesUiState.Error -> {
                Log.d("ShaqTagRoutes", state.message)
            }

            RoutesUiState.Init -> Unit
            is RoutesUiState.Loading -> {}
            is RoutesUiState.Success -> {
                Log.d("ShaqTagRoutes", state.routes.toString())
                Log.d("RoutesSize", state.routes.size.toString())
                state.routes.forEach { route ->
                    Log.d("RoutesPointSizePer1", route.points.size.toString())
                    val latLngList = route.points.map { points ->
                        LatLng(points.latitude, points.longitude)
                    }
                    drawAllRoutes(latLngList)
                }

            }
        }
    }

    private fun fetchRoutes() {
        viewModel.getAllRoutes()
    }

    private fun drawAllRoutes(pointList: List<LatLng>) {
        routePolyline =
            mMap.addPolyline(PolylineOptions().addAll(pointList).color(Color.BLUE))
    }

    private fun setButton() {
        binding.floatingActionButton.setOnClickListener {
            startActivity(Intent(this@RoutesActivity, MapsActivity::class.java))
            finish()
        }
    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {
        val request = DirectionsApi.getDirections(
            geoApiContext,
            "${origin.latitude},${origin.longitude}",
            "${destination.latitude},${destination.longitude}"
        ).mode(TravelMode.DRIVING).transitMode(TransitMode.BUS)

        try {
            val directionResult: DirectionsResult = request.await()

            Log.d(MapsActivity.TAG, "$directionResult")
            Log.d(MapsActivity.TAG, "ada draw")

            if (directionResult.routes.isNotEmpty()) {
                val route = directionResult.routes[0].overviewPolyline.decodePath()
                val routeLatLngList = route.map { LatLng(it.lat, it.lng) }

                routePolyline?.remove()

                routePolyline =
                    mMap.addPolyline(PolylineOptions().addAll(routeLatLngList).color(Color.BLUE))

            }
        } catch (e: Exception) {
            Log.e(MapsActivity.TAG, e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Shaq", "OnDestroy")
        handler.removeCallbacks(updateBusLocation)
    }

    companion object {
        const val NAME = "routes activity"
    }
}