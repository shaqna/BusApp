package com.maou.busapp.presentation.maps.callback

import android.content.Context
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.maou.busapp.R
import com.maou.busapp.presentation.maps.MapsActivity
import kotlin.math.roundToInt

class BusStopBottomSheetCallback(
    private val behavior: BottomSheetBehavior<ConstraintLayout>,
    private val maps: GoogleMap,
    private val context: Context
): BottomSheetCallback(){
    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_DRAGGING -> {
                Log.d(MapsActivity.TAG, "dragging")
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                // something
                bottomSheet.visibility = View.GONE
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                maps.setPadding(
                    0,
                    context.resources.getDimension(R.dimen.app_bar_height).roundToInt(),

                    0,
                    context.resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                )
            }
        }

    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        when(behavior.state) {
            BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                maps.setPadding(0,context.resources.getDimension(R.dimen.app_bar_height).roundToInt(), 0, (slideOffset * 1.0f).roundToInt())
            }
        }
    }
}