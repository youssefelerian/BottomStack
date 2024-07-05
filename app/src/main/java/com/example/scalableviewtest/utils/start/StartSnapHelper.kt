package com.example.scalableviewtest.utils.start

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class StartSnapHelper : LinearSnapHelper() {
    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        out[0] = 0
        out[1] = (layoutManager as YStackLayoutManager).getSnapHeight()
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        val custLayoutManager: YStackLayoutManager = layoutManager as YStackLayoutManager
        return custLayoutManager.findSnapView()
    }
}