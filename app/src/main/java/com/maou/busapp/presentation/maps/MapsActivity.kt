package com.maou.busapp.presentation.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Toast

import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TransitMode
import com.google.maps.model.TravelMode
import com.maou.busapp.R
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.databinding.ActivityMapsBinding
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.presentation.maps.adapters.BusEtaAdapter
import com.maou.busapp.presentation.home.BusStopAdapter
import com.maou.busapp.presentation.maps.callback.BusBottomSheetCallback
import com.maou.busapp.presentation.maps.callback.BusStopBottomSheetCallback
import com.maou.busapp.presentation.maps.callback.DetailsBottomSheetCallback
import com.maou.busapp.presentation.maps.callback.LocationBottomSheetCallback
import com.maou.busapp.presentation.routes.RoutesActivity
import com.maou.busapp.presentation.state.BusEtaUiState
import com.maou.busapp.presentation.state.BusStopUiState
import com.maou.busapp.presentation.state.LocationUiState
import com.maou.busapp.utils.BottomSheetHelper
import com.maou.busapp.utils.Constants
import com.maou.busapp.utils.GpsHelper
import com.maou.busapp.utils.PermissionHelper
import com.maou.busapp.utils.StringUtils
import com.maou.busapp.utils.Visibility
import com.maou.busapp.utils.convertTimeToStringMinute
import com.maou.busapp.utils.iconToBitmap
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.Locale
import kotlin.math.roundToInt


class MapsActivity : AppCompatActivity(),
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private lateinit var mMap: GoogleMap
    private lateinit var toolbar: Toolbar
    private val permissionHelper: PermissionHelper by inject()
    private val gpsHelper: GpsHelper by inject()
    private val viewModel: MapsViewModel by viewModel()
    private val bottomSheetHelper: BottomSheetHelper by inject()
    private val locationRequest: LocationRequest by inject()
    private var myLocationMarker: Marker? = null
    private var currentMapZoom: Float = 16f
    private val busStopMarkers: MutableList<Marker> = mutableListOf()
    private val busMarkers: MutableList<Marker> = mutableListOf()
    private var myLocation: Location? = null
    private var routePolyline: Polyline? = null

    private val binding: ActivityMapsBinding by lazy {
        ActivityMapsBinding.inflate(layoutInflater)
    }

    private val geoApiContext: GeoApiContext by lazy {
        GeoApiContext.Builder().apiKey(getString(R.string.lora_api_key)).build()
    }

    private val shimmerView: ShimmerFrameLayout by lazy {
        binding.bottomSheetPlaceHolder.shimmerViewContainer
    }


    private val busStopAdapter: BusStopAdapter by lazy {
        BusStopAdapter().apply {
            setItemSelected {
                val latLng = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                binding.busStopBottomSheetLayout.btnFindBus.apply {
                    text = StringUtils.generateText("Check bus in ${it.busStopName}")
                    isEnabled = true
                }

                viewModel.busStop = it
            }
        }
    }

    private val busEtaAdapter: BusEtaAdapter by lazy {
        BusEtaAdapter {
            bottomSheetHelper.run {
                expandDetailsAndHideBusSheet()
                setDetailsSheetVisibility(Visibility.VISIBLE)
                setBusSheetVisibility(Visibility.GONE)
            }
//            adjustMapPaddingToBottomSheet(
//                resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
//            )

            with(binding.detailsBottomSheetLayout) {
                tvBusCarPlate.text = StringUtils.generateText("Car Plate: ${it.busCarPlate}")
                tvCurrentBusStopName.text = viewModel.busStop?.busStopName

                val minutes = convertTimeToStringMinute(it.etaToCurrStop)

                if (minutes != 0L) {
                    tvPrevBusStop.visibility = View.VISIBLE
                    tvLastUpdateTime.visibility = View.VISIBLE
                    imgPrevBusStop.visibility = View.VISIBLE
                    tvPrevBusStop.text = it.currStopName
                    tvArrival.text = StringUtils.generateText("Status: Arrive in ${convertTimeToStringMinute(it.etaToCurrStop)} minutes")
                } else {
                    tvPrevBusStop.visibility = View.GONE
                    tvLastUpdateTime.visibility = View.GONE
                    imgPrevBusStop.visibility = View.GONE
                    tvArrival.text = StringUtils.generateText("Status: Arrived in the stop")
                }
            }

        }
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                Log.i(TAG, "onActivityResult: All location settings are satisfied.")
                viewModel.startRequestLocationUpdates()
            }

            RESULT_CANCELED ->
                Toast.makeText(
                    this@MapsActivity,
                    "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadKoinModules(MapsViewModel.inject())

        toolbar = binding.toolbar
        toolbar.title = ""
        setSupportActionBar(toolbar)

        //initBottomSheet()
        initMap()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // do enable GPS
        if (gpsHelper.isGpsEnable()) {
            Log.d(TAG, "requestLocationPermissions: GPS is active")
            viewModel.startRequestLocationUpdates()
        } else {
            enableGps()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied: $requestCode : ${perms.size}")
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(TAG, "onRationaleAccepted: $requestCode")

    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(TAG, "onRationaleDenied: $requestCode")

    }


    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

            initMapCamera()
            requestLocationPermissions()
            observeDeviceLocation()
            observeBusStop()
            //observeBusEta()
        }
    }

    private fun initBottomSheet() {
        bottomSheetHelper.run {
            initBusBottomSheet(binding)
            initBusStopBottomSheet(binding)
            initLocationBottomSheet(binding)
            initPlaceHolderBottomSheet(binding)
            initDetailsBottomSheet(binding)
        }
    }



    private fun requestBusStopByState() {
        when {
            viewModel.stateProvince?.contains(Constants.State.ACEH)!! -> {
                val request =
                    BusStopListRequest(
                        country = Constants.Country.IDN,
                        state = Constants.State.ACEH
                    )
                viewModel.fetchBusStops(request)
            }

            viewModel.stateProvince?.contains(Constants.State.PULAU_PINANG)!! -> {
                val request =
                    BusStopListRequest(
                        country = Constants.Country.MY,
                        state = Constants.State.PULAU_PINANG
                    )
                viewModel.fetchBusStops(request)
            }

            viewModel.stateProvince?.contains(Constants.State.JAWA_TIMUR)!! -> {
                val request = BusStopListRequest(
                    country = Constants.Country.IDN,
                    state = Constants.State.JAWA_TIMUR
                )
                viewModel.fetchBusStops(request)
            }

        }
    }

    private fun setButton() {
        binding.floatingActionButton.setOnClickListener {
            Intent(this@MapsActivity, RoutesActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }

        binding.locationBottomSheetLayout.apply {
            btnFindBusStop.setOnClickListener {
                bottomSheetHelper.run {
                    expandBusStopAndHideLocationSheet()
                    setBusStopSheetVisibility(Visibility.VISIBLE)
                    setLocationSheetVisibility(Visibility.GONE)
                }
                toolbar.run {
                    animate().translationY(-toolbar.height.toFloat()).setDuration(300)
                        .withEndAction {
                            visibility = View.GONE
                        }.start()
                }
                binding.appbar.appbarLayout.run {
                    visibility = View.VISIBLE
                    translationY = -toolbar.height.toFloat()
                    animate().translationY(0.0f).setDuration(300).start()
                }
                adjustMapPaddingToBottomSheet(
                    resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                )

                requestBusStopByState()
            }
        }

        binding.busStopBottomSheetLayout.apply {
            backBtn.setOnClickListener {
                bottomSheetHelper.run {
                    expandLocationAndHideBusStopSheet()
                    setLocationSheetVisibility(Visibility.VISIBLE)
                    setBusStopSheetVisibility(Visibility.GONE)
                }
                binding.appbar.appbarLayout.run {
                    animate().translationY(-toolbar.height.toFloat()).setDuration(300)
                        .withEndAction {
                            visibility = View.GONE
                        }.start()
                }
                toolbar.run {
                    visibility = View.VISIBLE
                    translationY = -toolbar.height.toFloat()
                    animate().translationY(0.0f).setDuration(300).start()
                }
                adjustMapPaddingToBottomSheet(
                    resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                )
                btnFindBus.apply {
                    text = getString(R.string.find_bus)
                    isEnabled = false
                }
                busStopMarkers.forEach {
                    it.remove()
                }
                busStopMarkers.clear()
                showMyLocation(myLocation!!)
            }



            btnFindBus.setOnClickListener {
                if (viewModel.busStop != null) {
                    bottomSheetHelper.run {
                        expandBusAndHideBusStopSheet()
                        setBusSheetVisibility(Visibility.VISIBLE)
                        setBusStopSheetVisibility(Visibility.GONE)
                    }
                    adjustMapPaddingToBottomSheet(
                        resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                    )
                    binding.appbar.appbarLayout.run {
                        animate().translationY(-toolbar.height.toFloat()).setDuration(300)
                            .withEndAction {
                                visibility = View.GONE
                            }.start()
                    }
                    viewModel.busStop?.let {
                        requestBusEtaInfo(it)
                        binding.busEtaBottomSheetLayout.tvBusStopName.text = it.busStopName
                    }
                }
            }
        }

        binding.busEtaBottomSheetLayout.apply {
            backBtn.setOnClickListener {
                bottomSheetHelper.run {
                    expandBusStopAndHideBusSheet()
                    setBusStopSheetVisibility(Visibility.VISIBLE)
                    setBusSheetVisibility(Visibility.GONE)
                }
                adjustMapPaddingToBottomSheet(
                    resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                )
                binding.appbar.appbarLayout.run {
                    visibility = View.VISIBLE
                    translationY = -toolbar.height.toFloat()
                    animate().translationY(0.0f).setDuration(300).start()
                }
                binding.busStopBottomSheetLayout.btnFindBus.text = getString(R.string.find_bus)
            }
        }

        binding.detailsBottomSheetLayout.apply {
            backBtn.setOnClickListener {
                bottomSheetHelper.run {
                    expandBusAndHideDetailsSheet()
                    setBusSheetVisibility(Visibility.VISIBLE)
                    setDetailsSheetVisibility(Visibility.GONE)
                }
                adjustMapPaddingToBottomSheet(
                    resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                )
            }
        }
    }

    private fun requestBusEtaInfo(busStop: BusStop) {
        when {
            viewModel.stateProvince?.contains(Constants.State.ACEH)!! -> {
                Log.d(TAG, busStop.busStopName)
                val busEtaRequest =
                    BusEtaRequest(
                        country = Constants.Country.IDN,
                        state = Constants.State.ACEH,
                        busStopName = busStop.busStopName
                    )
                viewModel.fetchBusEta(busEtaRequest)
            }

            viewModel.stateProvince?.contains(Constants.State.PULAU_PINANG)!! -> {
                Log.d(TAG, busStop.busStopName)
                val busEtaRequest =
                    BusEtaRequest(
                        country = Constants.Country.MY,
                        state = Constants.State.PULAU_PINANG,
                        busStopName = busStop.busStopName
                    )
                viewModel.fetchBusEta(busEtaRequest)
            }

            viewModel.stateProvince?.contains(Constants.State.JAWA_TIMUR)!! -> {
                Log.d(TAG, busStop.busStopName)
                val busEtaRequest =
                    BusEtaRequest(
                        country = Constants.Country.IDN,
                        state = Constants.State.JAWA_TIMUR,
                        busStopName = busStop.busStopName
                    )
                viewModel.fetchBusEta(busEtaRequest)
            }
        }
    }

    private fun observeBusEta() {
        viewModel.busEtaState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleBusEtaState(state)
            }
            .launchIn(lifecycleScope)
    }

    private fun handleBusEtaState(state: BusEtaUiState) {
        when (state) {
            is BusEtaUiState.Init -> Unit
            is BusEtaUiState.Loading -> {
                bottomSheetHelper.apply {
                    setBusSheetVisibility(Visibility.GONE)
                    setPlaceHolderSheetVisibility(Visibility.VISIBLE)
                }
                startShimmer()
            }

            is BusEtaUiState.Error -> {
                Log.d(TAG, state.message)
            }

            is BusEtaUiState.Success -> {
                stopShimmer()
                bottomSheetHelper.apply {
                    setBusSheetVisibility(Visibility.VISIBLE)
                    setPlaceHolderSheetVisibility(Visibility.GONE)
                }
                with(binding.busEtaBottomSheetLayout) {
                    rvBusEta.adapter = busEtaAdapter
                    rvBusEta.layoutManager = LinearLayoutManager(this@MapsActivity)

                    busEtaAdapter.setList(state.data)
                }
            }

            else -> {}
        }
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
            is BusStopUiState.Error -> Log.d(TAG, state.message)

            BusStopUiState.Init -> Unit
            is BusStopUiState.Loading -> {
                bottomSheetHelper.setBusStopSheetVisibility(Visibility.GONE)
                bottomSheetHelper.setPlaceHolderSheetVisibility(Visibility.VISIBLE)
//                startShimmer()
            }

            is BusStopUiState.Success -> {
                //stopShimmer()
                bottomSheetHelper.apply {
                    setPlaceHolderSheetVisibility(Visibility.GONE)
                    setBusStopSheetVisibility(Visibility.VISIBLE)
                }
//                adjustMapPaddingToBottomSheet(
//                    resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
//                )
                bottomSheetHelper.busStopBottomSheetBehavior.apply {
                    this?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                with(binding.busStopBottomSheetLayout) {
                    rvBusStop.apply {
                        adapter = busStopAdapter
                        layoutManager = LinearLayoutManager(this@MapsActivity)
                    }
                    Log.d("Bus stops", state.data.toString())
                    busStopAdapter.setList(state.data)
                    showBusStopLocation(state.data)
                }
            }
        }
    }

    private fun showBusStopLocation(data: List<BusStop>) {
        data.forEach { busStop: BusStop ->
            val latLng = LatLng(busStop.latitude.toDouble(), busStop.longitude.toDouble())
            val marker = mMap.addMarker(
                MarkerOptions().title(busStop.busStopName).position(latLng)
                    .icon(iconToBitmap(this@MapsActivity, R.drawable.ic_bus_stop))
            )

            marker?.let {
                busStopMarkers.add(it)
            }

        }
    }

    private fun observeDeviceLocation() {
        viewModel.locationState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleLocationState(state)
            }.launchIn(lifecycleScope)
    }

    private fun handleLocationState(state: LocationUiState) {
        when (state) {
            is LocationUiState.Loading -> {
                bottomSheetHelper.setPlaceHolderSheetVisibility(Visibility.VISIBLE)
            }

            is LocationUiState.OnLocationResult -> {
                state.location?.let {currentLocation ->
                    bottomSheetHelper.setPlaceHolderSheetVisibility(Visibility.GONE)
                    bottomSheetHelper.setLocationSheetVisibility(Visibility.VISIBLE)
                    bottomSheetHelper.locBottomSheetBehavior.apply {
                        this?.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    showMyLocation(currentLocation)
                    viewModel.stopRequestLocationUpdates()
                }
            }
        }
    }

    private fun startShimmer() {
        shimmerView.startShimmer()
//        adjustMapPaddingToBottomSheet(
//            resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
//        )
    }

    private fun stopShimmer() {
        shimmerView.stopShimmer()
//        adjustMapPaddingToBottomSheet(
//            resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
//        )
    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {
        val request = DirectionsApi.getDirections(
            geoApiContext,
            "${origin.latitude},${origin.longitude}",
            "${destination.latitude},${destination.longitude}"
        ).mode(TravelMode.DRIVING).transitMode(TransitMode.BUS)

        try {
            val directionResult: DirectionsResult = request.await()

            Log.d(TAG, "$directionResult")
            Log.d(TAG, "ada draw")

            if (directionResult.routes.isNotEmpty()) {
                val route = directionResult.routes[0].overviewPolyline.decodePath()
                val routeLatLngList = route.map { LatLng(it.lat, it.lng) }

                routePolyline?.remove()

                routePolyline =
                    mMap.addPolyline(PolylineOptions().addAll(routeLatLngList).color(Color.BLUE))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }


    private fun initMapCamera() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(USK_LatLng, 15f))
    }

    private fun setupBottomSheet() {

        adjustMapPaddingToBottomSheet(
            resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
        )

        bottomSheetHelper.locBottomSheetBehavior?.apply {
            isHideable = true
            addBottomSheetCallback(
                LocationBottomSheetCallback(
                    bottomSheetHelper.locBottomSheetBehavior!!,
                    mMap,
                    this@MapsActivity
                )
            )
        }
        bottomSheetHelper.busStopBottomSheetBehavior?.apply {
            isHideable = true
            addBottomSheetCallback(
                BusStopBottomSheetCallback(
                    bottomSheetHelper.busStopBottomSheetBehavior!!,
                    mMap,
                    this@MapsActivity
                )
            )
        }
        bottomSheetHelper.busBottomSheetBehavior?.apply {
            isHideable = true
            addBottomSheetCallback(
                BusBottomSheetCallback(
                    bottomSheetHelper.busBottomSheetBehavior!!,
                    mMap,
                    this@MapsActivity
                )
            )
        }

        bottomSheetHelper.detailsBottomSheetBehavior?.apply {
            isHideable = true
            addBottomSheetCallback(
                DetailsBottomSheetCallback(
                    bottomSheetHelper.busBottomSheetBehavior!!,
                    mMap,
                    this@MapsActivity
                )
            )
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun requestLocationPermissions() {
        if (permissionHelper.hasLocationPermission()) {
            // do enable GPS
            if (gpsHelper.isGpsEnable()) {
                Log.d(TAG, "requestLocationPermissions: GPS is active")
                viewModel.startRequestLocationUpdates()
            } else {
                enableGps()
            }
        } else {
            permissionHelper.requestLocationPermission(this, RC_LOCATION_PERM)
        }
    }

    private fun enableGps() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                Log.d(TAG, "LocationSettingsResponse: ${it.locationSettingsStates?.isGpsPresent}")
                viewModel.startRequestLocationUpdates()
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(e.resolution).build()
                        )

                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@MapsActivity, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    @SuppressLint("MissingPermission")
    private fun showMyLocation(location: Location) {
        myLocationMarker?.remove()
        myLocation = location

//        val myLatLng = LatLng(location.latitude, location.longitude)
        val myLatLng = viewModel.uskLocation

        mMap.apply {
            isMyLocationEnabled = true
            animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, currentMapZoom))
            myLocationMarker = mMap.addMarker(
                MarkerOptions().title("Your location").position(myLatLng)
                    .icon(iconToBitmap(this@MapsActivity, R.drawable.device_marker))
            )
        }

        currentMapZoom = mMap.cameraPosition.zoom

        val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("Versi Android", Build.VERSION.SDK_INT.toString())
            geocoder.getFromLocation(
//                location.latitude,
//                location.longitude
                myLatLng.latitude,
                myLatLng.longitude,
                1
            ) { addresses ->
                setAddresses(
                    addresses = addresses[0]
                )
            }
        } else {
            Log.d("Versi Android", Build.VERSION.SDK_INT.toString())
            val addresses = geocoder.getFromLocation(
//                location.latitude,
//                location.longitude
                myLatLng.latitude,
                myLatLng.longitude, 1
            )
            setAddresses(addresses!![0])
        }


    }

    private fun setAddresses(addresses: Address?) {
        with(binding.locationBottomSheetLayout) {
            val fullAddress = addresses?.getAddressLine(0)
            val city = addresses?.locality
            val state = addresses?.adminArea
            val postalCode = addresses?.postalCode
            val knownName = addresses?.featureName
            val subAdminArea = addresses?.subAdminArea
            val thoroughfare = addresses?.thoroughfare
            val subThoroughfare = addresses?.subThoroughfare
            val subLocality = addresses?.subLocality
            val country = addresses?.countryName

            viewModel.stateProvince = state
            Log.d(
                TAG, """
                    fullAddress = $fullAddress,
                    city = $city,
                    state = $state
                    postalCode = $postalCode
                    knownName = $knownName
                    subAdminArea = $subAdminArea
                    thoroughfare = $thoroughfare
                    subThoroughfare = $subThoroughfare
                    subLocality = $subLocality
                    country = $country
            """.trimIndent()
            )

            tvLocationName.text = city
            tvLocationStreet.text = fullAddress
            binding.tvAppBarTitle.text = StringUtils.generateText("Welcome to $city,$country")

            requestBusStopByState()
        }
    }


    private fun adjustMapPaddingToBottomSheet(bottomSheetHeight: Int) {

        Log.d("ShaqTagBottomSheetHeight", bottomSheetHeight.toString())
        Log.d("ShaqTagToolbarHeight", toolbar.height.toString())
        Log.d("ShaqTagAppHeight", binding.appbar.appbarLayout.height.toString())

        mMap.setPadding(
            0,
            binding.appbar.appbarLayout.height,
            0,
            bottomSheetHeight
        )
    }


    companion object {
        private val USK_LatLng = LatLng(5.570526082470679, 95.36978350794689)
        private const val RC_LOCATION_PERM = 111
        private val LOCATION_PERMS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        const val TAG = "ShaqTag"
    }
}