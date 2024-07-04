package com.example.scalableviewtest.utils

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import kotlin.math.pow


class YScrollLinearLayoutManager(context: Context,private val config: Config) : LinearLayoutManager(context) {
    private val overlapThreshold = config.maxStackCount
    private val overlapOffset = 30f

    private var mItemWidth = 0
    private var mItemHeight = 0

    init {
        orientation = RecyclerView.VERTICAL
    }

    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            applyOverlappingMode()
        }

    }

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        view.addOnScrollListener(recyclerViewOnScrollListener)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        try {
            val anchorView = recycler.getViewForPosition(0)
            measureChildWithMargins(anchorView, 0, 0)
            mItemWidth = anchorView.measuredWidth
            mItemHeight = anchorView.measuredHeight
        } catch (e: Exception) {

        }

    }

    private fun applyOverlappingMode() {
        val firstPosition = findFirstVisibleItemPosition()
        val lastVisibleItemPosition = findLastVisibleItemPosition()
        var firstOverlapThreshold = 0
        for (count in 0 until childCount) {
            val position = count + firstPosition
            val child = getChildAt(count)

            if (position > (lastVisibleItemPosition - overlapThreshold) && position <= lastVisibleItemPosition + 1) {

                if (child != null) {
                    val newY =
                        ((count * overlapOffset) + (firstOverlapThreshold * (mItemHeight / 2)))
                    child.translationY = -newY
                    child.scaleX = 0.99.pow(count).toFloat()
                    child.scaleY = 0.97.pow(count).toFloat()
                    child.alpha = 0.9f
                    child.z = (-position * 2).toFloat()
                }
                firstOverlapThreshold++
            } else {
                // Log.w("YYYYYYYYYY", "position = $position  -- resetLayout")
                resetLayout(child)
            }
        }
    }

    private fun resetLayout() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            resetLayout(child)

        }
    }

    private fun resetLayout(child: View?) {
        if (child != null) {
            child.translationY = 0f
            child.scaleX = 1f
            child.scaleY = 1f
            child.alpha = 1f
            child.z = 0f
        }
    }
}
