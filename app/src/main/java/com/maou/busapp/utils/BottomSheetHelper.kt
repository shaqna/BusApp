package com.maou.busapp.utils

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.maou.busapp.databinding.ActivityMapsBinding

enum class Visibility { VISIBLE, GONE }

class BottomSheetHelper {
    private var locBottomSheetLayout: ConstraintLayout? = null
    var locBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private var busStopBottomSheetLayout: ConstraintLayout? = null
    var busStopBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private var busBottomSheetLayout: ConstraintLayout? = null
    var busBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    var detailsBottomSheetLayout: ConstraintLayout? = null
    var detailsBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private var placeHolderBottomSheetLayout: ConstraintLayout? = null

    fun initLocationBottomSheet(binding: ActivityMapsBinding) {
        locBottomSheetLayout = binding.locationBottomSheetLayout.locationBottomSheet
        locBottomSheetBehavior = BottomSheetBehavior.from(locBottomSheetLayout!!)
    }

    fun initBusStopBottomSheet(binding: ActivityMapsBinding) {
        busStopBottomSheetLayout = binding.busStopBottomSheetLayout.busStopBottomSheet
        busStopBottomSheetBehavior = BottomSheetBehavior.from(busStopBottomSheetLayout!!)
    }

    fun initBusBottomSheet(binding: ActivityMapsBinding) {
        busBottomSheetLayout = binding.busEtaBottomSheetLayout.busBottomSheet
        busBottomSheetBehavior = BottomSheetBehavior.from(busBottomSheetLayout!!)
    }

    fun initPlaceHolderBottomSheet(binding: ActivityMapsBinding) {
        placeHolderBottomSheetLayout = binding.bottomSheetPlaceHolder.placeHolder
    }

    fun initDetailsBottomSheet(binding: ActivityMapsBinding) {
        detailsBottomSheetLayout = binding.detailsBottomSheetLayout.detailsBottomSheet
        detailsBottomSheetBehavior = BottomSheetBehavior.from(detailsBottomSheetLayout!!)
    }

    fun expandBusStopAndHideLocationSheet() {
        locBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        busStopBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun expandLocationAndHideBusStopSheet() {
        busStopBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        locBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun expandBusAndHideBusStopSheet() {
        busStopBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        busBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun expandBusStopAndHideBusSheet() {
        busBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        busStopBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun expandDetailsAndHideBusSheet() {
        busBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        detailsBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun expandBusAndHideDetailsSheet() {
        detailsBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        busBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setLocationSheetVisibility(visibility: Visibility) {
        when(visibility) {
            Visibility.VISIBLE -> locBottomSheetLayout?.visibility = View.VISIBLE
            Visibility.GONE -> locBottomSheetLayout?.visibility = View.GONE
        }
    }

    fun setBusStopSheetVisibility(visibility: Visibility) {
        when(visibility) {
            Visibility.VISIBLE -> busStopBottomSheetLayout?.visibility = View.VISIBLE
            Visibility.GONE -> busStopBottomSheetLayout?.visibility = View.GONE
        }
    }

    fun setBusSheetVisibility(visibility: Visibility) {
        when(visibility) {
            Visibility.VISIBLE -> busBottomSheetLayout?.visibility = View.VISIBLE
            Visibility.GONE -> busBottomSheetLayout?.visibility = View.GONE
        }
    }

    fun setPlaceHolderSheetVisibility(visibility: Visibility) {
        when(visibility) {
            Visibility.VISIBLE -> placeHolderBottomSheetLayout?.visibility = View.VISIBLE
            Visibility.GONE -> placeHolderBottomSheetLayout?.visibility = View.GONE
        }
    }

    fun setDetailsSheetVisibility(visibility: Visibility) {
        when(visibility) {
            Visibility.VISIBLE -> detailsBottomSheetLayout?.visibility = View.VISIBLE
            Visibility.GONE -> detailsBottomSheetLayout?.visibility = View.GONE
        }
    }
}