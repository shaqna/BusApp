package com.maou.busapp.presentation.maps.callback

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.maou.busapp.R
import kotlin.math.roundToInt

class DetailsBottomSheetCallback(
    private val behavior: BottomSheetBehavior<ConstraintLayout>,
    private val maps: GoogleMap,
    private val context: Context
): BottomSheetBehavior.BottomSheetCallback()  {
    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_DRAGGING -> {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                bottomSheet.visibility = View.GONE
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                maps.setPadding(
                    0,
                    0,
                    0,
                    context.resources.getDimension(R.dimen.bottom_sheet_height).roundToInt()
                )
            }
        }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        when(behavior.state) {
            BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                maps.setPadding(0,0, 0, (slideOffset * 1.0f).roundToInt())
            }
        }
    }
}