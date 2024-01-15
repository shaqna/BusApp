package com.maou.busapp.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.maou.busapp.R
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.databinding.ActivityHomeBinding
import com.maou.busapp.presentation.maps.MapsActivity
import com.maou.busapp.presentation.routes.RoutesActivity
import com.maou.busapp.presentation.state.BusStopUiState
import com.maou.busapp.presentation.state.LocationUiState
import com.maou.busapp.utils.Constants
import com.maou.busapp.utils.GpsHelper
import com.maou.busapp.utils.PermissionHelper
import com.maou.busapp.utils.StringUtils
import com.maou.busapp.utils.Visibility
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

class HomeActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val locationRequest: LocationRequest by inject()
    private val permissionHelper: PermissionHelper by inject()
    private val gpsHelper: GpsHelper by inject()
    private var myLocation: Location? = null

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private val viewModel: HomeViewModel by viewModel()

    private val busStopAdapter: BusStopAdapter by lazy {
        BusStopAdapter().apply {
            setItemSelected {
                val latLng = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                viewModel.busStop = it

                Intent(this@HomeActivity, RoutesActivity::class.java).also {intent ->
                    startActivity(intent)
                }
            }
        }
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                Log.i(MapsActivity.TAG, "onActivityResult: All location settings are satisfied.")
                viewModel.startRequestLocationUpdates()
            }

            RESULT_CANCELED ->
                Toast.makeText(
                    this@HomeActivity,
                    "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadKoinModules(HomeViewModel.inject())

        setButton()
        requestLocationPermissions()
        observeDeviceLocation()
        observeBusStop()

    }

    private fun setButton() {
        with(binding) {
            fabMap.setOnClickListener {
                startActivity(Intent(this@HomeActivity, MapsActivity::class.java))
            }
        }
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
        if (gpsHelper.isGpsEnable()) {
            Log.d(MapsActivity.TAG, "requestLocationPermissions: GPS is active")
            viewModel.startRequestLocationUpdates()
        } else {
            enableGps()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(MapsActivity.TAG, "onPermissionsDenied: $requestCode : ${perms.size}")
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(MapsActivity.TAG, "onRationaleAccepted: $requestCode")

    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(MapsActivity.TAG, "onRationaleDenied: $requestCode")

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
                Log.d(
                    MapsActivity.TAG,
                    "LocationSettingsResponse: ${it.locationSettingsStates?.isGpsPresent}"
                )
                viewModel.startRequestLocationUpdates()
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(e.resolution).build()
                        )

                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@HomeActivity, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
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
                startShimmer()
            }

            is LocationUiState.OnLocationResult -> {
                state.location?.let { currentLocation ->
                    stopShimmer()
                    showMyLocation(currentLocation)
                    viewModel.stopRequestLocationUpdates()

                }
            }
        }
    }

    private fun startShimmer() {
        with(binding) {
            val shimmerWelcome: ShimmerFrameLayout = tvShimmerWelcome
            val shimmerAvailable: ShimmerFrameLayout = tvAvailableShimmer
            val shimmerRvStops: ShimmerFrameLayout = tvRvShimmer
            val shimmerState: ShimmerFrameLayout = tvStateShimmer

            tvWelcome.visibility = View.GONE
            tvState.visibility = View.GONE
            tvAvailable.visibility = View.GONE
            rvStops.visibility = View.GONE

            shimmerAvailable.startShimmer()
            shimmerRvStops.startShimmer()
            shimmerState.startShimmer()
            shimmerWelcome.startShimmer()
        }
    }

    private fun stopShimmer() {
        with(binding) {
            val shimmerWelcome: ShimmerFrameLayout = tvShimmerWelcome
            val shimmerAvailable: ShimmerFrameLayout = tvAvailableShimmer
            val shimmerRvStops: ShimmerFrameLayout = tvRvShimmer
            val shimmerState: ShimmerFrameLayout = tvStateShimmer

            shimmerAvailable.stopShimmer()
            shimmerRvStops.stopShimmer()
            shimmerState.stopShimmer()
            shimmerWelcome.stopShimmer()

            shimmerAvailable.visibility = View.GONE
            shimmerRvStops.visibility = View.GONE
            shimmerState.visibility = View.GONE
            shimmerWelcome.visibility = View.GONE

            tvWelcome.visibility = View.VISIBLE
            tvState.visibility = View.VISIBLE
            tvAvailable.visibility = View.VISIBLE
            rvStops.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun showMyLocation(location: Location) {
        myLocation = location

//        val myLatLng = LatLng(location.latitude, location.longitude)
        val myLatLng = viewModel.uskLocation


        val geocoder = Geocoder(this@HomeActivity, Locale.getDefault())

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
        with(binding) {
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
            fetchBusStopsByState()
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

            tvState.text = state
        }
    }

    private fun fetchBusStopsByState() {
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
            is BusStopUiState.Loading -> {
                showLoading(true)
            }

            is BusStopUiState.Success -> {
                showLoading(false)
                with(binding) {
                    rvStops.apply {
                        adapter = busStopAdapter
                        layoutManager = LinearLayoutManager(this@HomeActivity)
                    }
                    Log.d("Bus stops", state.data.toString())
                    busStopAdapter.setList(state.data)
                }
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        if(loading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }

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