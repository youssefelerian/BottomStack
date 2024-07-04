package com.example.scalableviewtest.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler


class YStackLayoutManager(private val config: Config, private val recyclerView: RecyclerView) :
    LinearLayoutManager(recyclerView.context) {

    init {
        orientation = RecyclerView.VERTICAL
    }

    private var maxStackCount = config.maxStackCount
    private var mSpace = config.space
    private var initialStackCount = config.initialStackCount
    private var secondaryScale = config.secondaryScale
    private var scaleRatio = config.scaleRatio
    private var parallex = config.parallax


    private var mItemWidth = 0
    private var mItemHeight = 0


    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            changeScaleAndPosition()
        }

    }


    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        view.addOnScrollListener(recyclerViewOnScrollListener)
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        if (itemCount <= 0) return

        val anchorView = recycler.getViewForPosition(0)
        measureChildWithMargins(anchorView, 0, 0)
        mItemWidth = anchorView.measuredWidth
        mItemHeight = anchorView.measuredHeight

        changeScaleAndPosition()
    }

    private fun changeScaleAndPosition() {
        // Find the last visible item position
        val lastVisibleItemPosition = findLastVisibleItemPosition()

        // Loop through all child views
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)
            val scale = scale(position)
            Log.w(
                "UUUUUUUUUUU",
                "pos = $position - child.bottom = ${child.bottom}  "
            )
            // Apply scaling and position adjustment to the last visible item
            if (position == lastVisibleItemPosition) {
                child.scaleX = scale
                child.scaleY = scale

                // Move the last item to the position of the previous item

                val prevChild = getChildAt(i - 1)
                if (prevChild != null) {

                    val overlapAmount = mItemHeight / 2 // Adjust this for the amount of overlap

                    val newTop = prevChild.bottom - overlapAmount
                   // child.layout(child.left, newTop, child.right, newTop + child.measuredHeight)
                    // Ensure the last item is behind the previous item
                    prevChild.translationZ = 1f
                    child.translationZ = 0f
                }
            } else {
                // Reset scaling for other items
                child.scaleX = scale
                child.scaleY = scale
                val newTop = (position+1) * mItemHeight
               // child.layout(child.left, newTop, child.right, newTop + child.measuredHeight)
                child.translationZ = 1f
            }
        }
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        Log.i(TAG, "onLayoutCompleted   state = $state")
        super.onLayoutCompleted(state)

    }


    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?, newAdapter: RecyclerView.Adapter<*>?
    ) {
        Log.i(TAG, "onAdapterChanged  ")

    }

    override fun requestLayout() {
        Log.i(TAG, "requestLayout  ")
        super.requestLayout()
    }

    private fun alpha(position: Int): Float {
        val lastVisibleItemPosition = findLastVisibleItemPosition()
        return if (position == lastVisibleItemPosition) {
            0.8f
        } else {
            // Reset scaling for other items
            1.0f
        }
    }

    private fun scale(position: Int): Float {
        val lastVisibleItemPosition = findLastVisibleItemPosition()
        return if (position == lastVisibleItemPosition) {
            0.7f
        } else {
            // Reset scaling for other items
            1.0f
        }
    }


    companion object {
        const val TAG = "YStackLayoutManager"
    }
}