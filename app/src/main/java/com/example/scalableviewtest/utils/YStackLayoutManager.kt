package com.example.scalableviewtest.utils

import android.graphics.PointF
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.pow


class YStackLayoutManager(recyclerView: RecyclerView,private val config: Config) :
    LinearLayoutManager(recyclerView.context) {

    init {
        orientation = RecyclerView.VERTICAL
    }

    private var isOverlappingMode = true
    private val overlapThreshold = 3
    private val overlapOffset = 10f
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        if (isOverlappingMode) {
            applyOverlappingMode(recycler)
        } else {
            resetLayout(recycler)
        }
    }

    private fun applyOverlappingMode(recycler: RecyclerView.Recycler?) {
        val itemCount = itemCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null) {
                val position = getPosition(child)
                if (position < overlapThreshold) {
                    val newY = position * overlapOffset
                    child.translationY = newY
                    child.scaleX = 0.9.pow(position.toDouble()).toFloat()
                    child.scaleY = 0.9.pow(position.toDouble()).toFloat()
                    child.alpha = max(0.0, (1 - 0.05f * position).toDouble()).toFloat()
                    child.z = (-position * 2).toFloat()
                } else {
                    child.alpha = 0f
                }
            }
        }
    }

    private fun resetLayout(recycler: RecyclerView.Recycler?) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null) {
                child.translationY = 0f
                child.scaleX = 1f
                child.scaleY = 1f
                child.alpha = 1f
                child.z = 0f
            }
        }
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        return super.computeScrollVectorForPosition(targetPosition)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == RecyclerView.SCROLL_STATE_IDLE && isOverlappingMode) {
            applyOverlappingMode(null)
        }
    }

    fun toggleLayout() {
        isOverlappingMode = !isOverlappingMode
        requestLayout()
    }

    companion object {
        const val TAG = "YStackLayoutManager"
    }
}