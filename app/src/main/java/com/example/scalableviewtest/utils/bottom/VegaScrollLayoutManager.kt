package com.example.scalableviewtest.utils.bottom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scalableviewtest.utils.Config
import kotlin.math.max
import kotlin.math.pow

class VegaScrollLayoutManager(
    private val recyclerView: RecyclerView, private val config: Config
) : LinearLayoutManager(recyclerView.context) {

    private var isOverlappingMode = true
    private var overlapThreshold = 3
    private val overlapOffset = 10f

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        detachAndScrapAttachedViews(recycler)
        layoutViews(recycler)
    }

    private fun layoutViews(recycler: RecyclerView.Recycler) {
        var offsetY = paddingTop
        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)

            if (isOverlappingMode) {
                val newY = i * overlapOffset.toInt() + paddingTop
                val newWidth = (width * 0.9.pow(i.toDouble())).toInt()
                val newX = (width - newWidth) / 2
                layoutDecorated(view, newX, newY, newX + newWidth, newY + height)
                view.translationZ = (-i * 2).toFloat()
                view.alpha = if (i < overlapThreshold) max(0f, 1 - 0.05f * i) else 0f
            } else {
                layoutDecorated(view, paddingLeft, offsetY, paddingLeft + width, offsetY + height)
                offsetY += height + overlapOffset.toInt()
            }
        }
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }

        val travel = fill(dy, recycler)
        offsetChildrenVertical(-travel)
        recycleViewsOutOfBounds(dy, recycler)
        return travel
    }

    private fun fill(dy: Int, recycler: RecyclerView.Recycler): Int {
        var delta = dy

        if (delta > 0) {
            // Scrolling up
            while (childCount > 0) {
                val lastChild = getChildAt(childCount - 1) ?: break
                if (getDecoratedBottom(lastChild) - delta >= height) {
                    removeAndRecycleView(lastChild, recycler)
                } else {
                    break
                }
            }

            var anchorView: View? = null
            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i) ?: continue
                if (getDecoratedBottom(child) <= height) {
                    anchorView = child
                    break
                }
            }

            var pos = getPosition(anchorView ?: getChildAt(childCount - 1)!!)
            while (pos < itemCount) {
                val view = recycler.getViewForPosition(pos)
                addView(view)
                measureChildWithMargins(view, 0, 0)
                val width = getDecoratedMeasuredWidth(view)
                val height = getDecoratedMeasuredHeight(view)

                val bottom = if (anchorView == null) {
                    paddingTop + height
                } else {
                    getDecoratedBottom(anchorView) + overlapOffset.toInt()
                }

                layoutDecorated(view, paddingLeft, bottom - height, paddingLeft + width, bottom)

                if (getDecoratedBottom(view) >= height) {
                    break
                }
                anchorView = view
                pos++
            }

        } else if (delta < 0) {
            // Scrolling down
            while (childCount > 0) {
                val firstChild = getChildAt(0) ?: break
                if (getDecoratedTop(firstChild) - delta <= 0) {
                    removeAndRecycleView(firstChild, recycler)
                } else {
                    break
                }
            }

            var anchorView: View? = null
            for (i in 0 until childCount) {
                val child = getChildAt(i) ?: continue
                if (getDecoratedTop(child) >= 0) {
                    anchorView = child
                    break
                }
            }

            var pos = getPosition(anchorView ?: getChildAt(0)!!)
            while (pos >= 0) {
                val view = recycler.getViewForPosition(pos)
                addView(view, 0)
                measureChildWithMargins(view, 0, 0)
                val width = getDecoratedMeasuredWidth(view)
                val height = getDecoratedMeasuredHeight(view)

                val top = if (anchorView == null) {
                    height - paddingTop
                } else {
                    getDecoratedTop(anchorView) - overlapOffset.toInt()
                }

                layoutDecorated(view, paddingLeft, top, paddingLeft + width, top + height)

                if (getDecoratedTop(view) <= 0) {
                    break
                }
                anchorView = view
                pos--
            }
        }

        return delta
    }

    private fun recycleViewsOutOfBounds(dy: Int, recycler: RecyclerView.Recycler) {
        if (dy > 0) {
            // Recycle views that went off the top
            while (childCount > 0) {
                val child = getChildAt(0) ?: break
                if (getDecoratedBottom(child) <= 0) {
                    removeAndRecycleView(child, recycler)
                } else {
                    break
                }
            }
        } else if (dy < 0) {
            // Recycle views that went off the bottom
            while (childCount > 0) {
                val child = getChildAt(childCount - 1) ?: break
                if (getDecoratedTop(child) >= height) {
                    removeAndRecycleView(child, recycler)
                } else {
                    break
                }
            }
        }
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
    }



    fun toggleLayout() {
        isOverlappingMode = !isOverlappingMode
        requestLayout()
    }
}
