package com.example.scalableviewtest.final_layout

import android.content.Context
import android.graphics.Rect
import android.util.ArrayMap
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

/**
 * Created by Youssef Ebrahim Elerian on 4/6/2024.
 * youssef.elerian@gmail.com
 */

class BottomStackLayoutManager(context: Context, private val config: BottomStackConfig) :
    LinearLayoutManager(context) {

    init {
        orientation = RecyclerView.VERTICAL

    }

    private var scroll = 0
    private val locationRects: SparseArray<Rect> = SparseArray<Rect>()
    private val attachedItems = SparseBooleanArray()
    private val viewTypeHeightMap: ArrayMap<Int, Int> = ArrayMap()

    private var needSnap = false
    private var lastDy = 0
    private var maxScroll = -1
    private var adapter: RecyclerView.Adapter<*>? = null
    private var recycler: RecyclerView.Recycler? = null
    private var totalVisibleItemCount = 0

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        this.adapter = newAdapter
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        this.recycler = recycler

        if (state.isPreLayout) {
            return
        }
        buildLocationRects()

        detachAndScrapAttachedViews(recycler)
        layoutItemsOnCreate(recycler)
    }

    override fun onScrollStateChanged(state: Int) {
        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            needSnap = true
        }
        super.onScrollStateChanged(state)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State
    ): Int {
        if (itemCount == 0 || dy == 0) {
            return 0
        }
        var travel = dy
        if (dy + scroll < 0) {
            travel = -scroll
        } else if (dy + scroll > maxScroll) {
            travel = maxScroll - scroll
        }
        scroll += travel
        lastDy = dy
        if (!state.isPreLayout && childCount > 0) {
            layoutItemsOnScroll()
        }
        return travel
    }

    private fun buildLocationRects() {
        locationRects.clear()
        attachedItems.clear()

        var tempPosition = paddingTop
        val itemCount = itemCount
        for (i in 0 until itemCount) {

            val viewType = adapter?.getItemViewType(i)
            var itemHeight = 0
            if (viewTypeHeightMap.containsKey(viewType)) {
                itemHeight = viewTypeHeightMap[viewType] ?: 0
            } else {
                recycler?.getViewForPosition(i)?.let { itemView ->
                    addView(itemView)
                    measureChildWithMargins(
                        itemView,
                        View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED
                    )
                    itemHeight = getDecoratedMeasuredHeight(itemView)
                    viewTypeHeightMap[viewType] = itemHeight
                }
            }

            val rect = Rect()
            rect.left = paddingLeft
            rect.top = tempPosition
            rect.right = width - paddingRight
            rect.bottom = rect.top + itemHeight
            locationRects.put(i, rect)
            attachedItems.put(i, false)
            tempPosition += itemHeight
        }

        if (itemCount == 0) {
            maxScroll = 0
        } else {
            computeMaxScroll()
        }
    }

    private fun computeMaxScroll() {
        maxScroll = locationRects[locationRects.size() - 1].bottom - height
        if (maxScroll < 0) {
            maxScroll = 0
            return
        }

        val itemCount = itemCount
        var screenFilledHeight = 0
        for (i in itemCount - 1 downTo 0) {
            val rect: Rect = locationRects[i]
            screenFilledHeight += (rect.bottom - rect.top)
            if (screenFilledHeight > height) {
                val extraSnapHeight: Int = height - (screenFilledHeight - (rect.bottom - rect.top))
                maxScroll += extraSnapHeight
                break
            }
        }

    }

    private fun layoutItemsOnCreate(recycler: RecyclerView.Recycler) {
        val itemCount = itemCount
        val displayRect = Rect(0, scroll, width, height + scroll)
        for (i in 0 until itemCount) {
            val thisRect: Rect = locationRects[i]
            if (Rect.intersects(displayRect, thisRect)) {
                val childView = recycler.getViewForPosition(i)
                addView(childView)
                measureChildWithMargins(
                    childView,
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )
                layoutItem(childView, locationRects[i], i)
                attachedItems.put(i, true)
                childView.pivotY = 0f
                childView.pivotX = (childView.measuredWidth / 2).toFloat()
                if (i > totalVisibleItemCount) {
                    totalVisibleItemCount = i
                }
                if (thisRect.top - scroll > height) {
                    break
                }

            }
        }
    }

    private fun layoutItemsOnScroll() {
        val childCount = childCount
        val itemCount = itemCount
        val displayRect = Rect(0, scroll, width, height + scroll)
        var firstVisiblePosition = -1
        var lastVisiblePosition = -1
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)
            if (!Rect.intersects(displayRect, locationRects[position])) {

                removeAndRecycleView(child, recycler!!)
                attachedItems.put(position, false)
            } else {

                if (lastVisiblePosition < 0) {
                    lastVisiblePosition = position
                }

                firstVisiblePosition = if (firstVisiblePosition < 0) {
                    position
                } else {
                    min(firstVisiblePosition.toDouble(), position.toDouble()).toInt()
                }

                layoutItem(child, locationRects[position], position)
            }
        }

        if (firstVisiblePosition > 0) {

            for (i in firstVisiblePosition - 1 downTo 0) {
                if (Rect.intersects(displayRect, locationRects[i]) &&
                    !attachedItems[i]
                ) {
                    reuseItemOnSroll(i, true)
                } else {
                    break
                }
            }
        }

        for (i in lastVisiblePosition + 1 until itemCount) {
            if (Rect.intersects(displayRect, locationRects[i]) &&
                !attachedItems[i]
            ) {
                reuseItemOnSroll(i, false)
            } else {
                break
            }
        }

    }

    private fun reuseItemOnSroll(position: Int, addViewFromTop: Boolean) {
        val scrap = recycler!!.getViewForPosition(position)
        measureChildWithMargins(scrap, 0, 0)
        scrap.pivotY = 0f
        scrap.pivotX = (scrap.measuredWidth / 2).toFloat()

        if (addViewFromTop) {
            addView(scrap, 0)
        } else {
            addView(scrap)
        }

        layoutItem(scrap, locationRects[position], position)
        attachedItems.put(position, true)
    }


    private fun layoutItem(child: View, rect: Rect, position: Int) {
        val layoutScrollTop = rect.top - scroll
        val layoutScrollBottom = rect.bottom - scroll
        val itemHeight: Int = rect.bottom - rect.top
        val layoutTopNeed = (itemHeight * (totalVisibleItemCount - 1)) - (itemHeight / 2)

        val layoutTop: Int
        val layoutBottom: Int
        if (position != 0 && totalVisibleItemCount != 0 && position + 1 >= totalVisibleItemCount && layoutScrollTop >= layoutTopNeed) {

            val rateInit = (layoutScrollTop - layoutTopNeed).toFloat() / itemHeight.toFloat()
            val scaleFactor = 1 - rateInit * rateInit / 3
            val heightRate = (rateInit * itemHeight.toFloat()).toInt()
            child.scaleX = scaleFactor
            child.scaleY = scaleFactor
            child.z = -rateInit
            if (config.isAlpha) {
                val rate3 = 1 - rateInit * rateInit
                child.alpha = rate3
            }
            layoutTop = layoutScrollTop - heightRate - config.space
            layoutBottom = layoutScrollBottom - heightRate - config.space

        } else {
            child.scaleX = 1f
            child.scaleY = 1f
            child.alpha = 1f
            child.z = 0f
            layoutTop = layoutScrollTop
            layoutBottom = layoutScrollBottom
        }
        layoutDecorated(child, rect.left, layoutTop, rect.right, layoutBottom)

    }

    companion object {
        const val TAG = "YOUSSEF"
    }
}

data class BottomStackConfig(
    val isAlpha: Boolean = false,
    val stackCount: Int = 1,
    val space: Int = 40
)