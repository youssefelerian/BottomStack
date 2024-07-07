package com.example.scalableviewtest.utils.bottom

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.scalableviewtest.utils.start.Config
import kotlin.math.pow


class YScrollBottomManager(
    private val recyclerView: RecyclerView, private val config: Config
) : LinearLayoutManager(recyclerView.context) {
    private val overlapThreshold = config.maxStackCount
    private val overlapOffset = config.space

    private var mItemWidth = 0
    private var mItemHeight = 0
    private var recycler: Recycler? = null

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
        this.recycler = recycler
        try {
            val anchorView = recycler.getViewForPosition(0)
            measureChildWithMargins(anchorView, 0, 0)
            mItemWidth = anchorView.measuredWidth
            mItemHeight = anchorView.measuredHeight

        } catch (_: Exception) {

        }

    }

    private fun applyOverlappingMode() {
        val firstPosition = findFirstVisibleItemPosition()
        var firstOverlapThreshold = 0
        for (count in 0 until childCount) {
            val position = count + firstPosition
            val child = getChildAt(count)
            if (isLastItemsVisible(position) && !isLastItemsInList(position)) {
                firstOverlapThreshold++
                changeScaleAndPosition(
                    child = child,
                    count = count,
                    position = position,
                    firstOverlapThreshold = firstOverlapThreshold
                )

            } else {
                resetLayout(child, position)
            }
        }
    }

    private fun isLastItemsVisible(position: Int): Boolean {
        val lastVisibleItemPosition = findLastVisibleItemPosition()
        val isLastItemsVisible =
            position > (lastVisibleItemPosition - overlapThreshold) && position <= lastVisibleItemPosition + 1
        return isLastItemsVisible
    }

    private fun isLastItemsInList(position: Int): Boolean {
        val isLastItemsInList = position + overlapThreshold >= itemCount
        return isLastItemsInList
    }

    private fun newY(firstOverlapThreshold: Int): Float {
        return ((mItemHeight * 3 / 4) * firstOverlapThreshold + (firstOverlapThreshold - 1 * overlapOffset)).toFloat()
    }

    private fun changeScaleAndPosition(
        child: View?,
        count: Int,
        position: Int,
        firstOverlapThreshold: Int
    ) {
        if (child != null) {
            val scaleXV = 0.99.pow(count).toFloat()
            val scaleYV = 0.97.pow(count).toFloat()
            val translationY = -newY(firstOverlapThreshold)
            val translationZ = (-position * 2).toFloat()
            val alpha = 0.9f
            child.translationY = translationY
            child.scaleX = scaleXV
            child.scaleY = scaleYV
            child.alpha = alpha
            child.translationZ = translationZ
            /* removeAndRecycleView(child, recycler!!)
             val view = recycler!!.getViewForPosition(position)
             addView(view)
             measureChildWithMargins(view, 0, 0)*/
        }
    }

    private fun resetLayout() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            resetLayout(child, i)

        }
    }

    private fun resetLayout(child: View?, position: Int) {
        if (child != null) {
            // if (position == findLastVisibleItemPosition()-1) {

            /* child.animate().translationZ(0f)
                 .translationY(0f)
                 .scaleX(1f).scaleY(1f)
                 .alpha(1f)
                 .start()*/

            child.translationY = 0f
            child.translationZ = 0f
            child.scaleX = 1f
            child.scaleY = 1f
            child.alpha = 1f
            //}
        }
    }

}
