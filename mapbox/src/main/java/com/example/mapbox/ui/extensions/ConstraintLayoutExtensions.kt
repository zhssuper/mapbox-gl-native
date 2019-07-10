package com.example.mapbox.ui.extensions

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.View

fun ConstraintLayout.addViewAndMatchItToParent(view: View, zIndex: Int = 0) {
    addView(view, zIndex)
    ConstraintSet().run {
        clone(this@addViewAndMatchItToParent)
        view.run {
            connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }
        applyTo(this@addViewAndMatchItToParent)
    }
}
