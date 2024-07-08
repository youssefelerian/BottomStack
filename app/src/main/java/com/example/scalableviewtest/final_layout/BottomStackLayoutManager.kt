package com.example.scalableviewtest.final_layout

import android.graphics.Rect
import android.util.ArrayMap
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min


/**
 * Created by Youssef Ebrahim Elerian on 4/6/2024.
 * youssef.elerian@gmail.com
 */

class BottomStackLayoutManager(private val recyclerView: RecyclerView) :
    LinearLayoutManager(recyclerView.context) {

    private val stackCount: Int = 1
    private val isAlpha: Boolean = false

    init {
        orientation = RecyclerView.VERTICAL
    }

    private var recyclerViewChangeHeight = 0
    private var recyclerViewDefaultHeight = 0
    private var scroll = 0
    private val locationRects: SparseArray<Rect> = SparseArray<Rect>()
    private val attachedItems = SparseBooleanArray()
    private val viewTypeHeightMap: ArrayMap<Int, Int> = ArrayMap()

    private var maxScroll = -1
    private var adapter: RecyclerView.Adapter<*>? = null
    private var recycler: RecyclerView.Recycler? = null
    private var totalVisibleItemCount = 0
    private var itemHeight = 0

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
        try {
            val anchorView = recycler.getViewForPosition(0)
            measureChildWithMargins(anchorView, 0, 0)
            itemHeight = anchorView.measuredHeight
            recyclerViewChangeHeight = recyclerView.height

            if (recyclerViewDefaultHeight == 0) {
                recyclerViewDefaultHeight = recyclerView.height
            }
            if (itemHeight > 0 && recyclerViewChangeHeight > 0) {
                totalVisibleItemCount =
                    (recyclerViewChangeHeight.toFloat() / itemHeight.toFloat()).toInt() + 1
            }
        } catch (_: Exception) {

        }

        buildLocationRects()

        detachAndScrapAttachedViews(recycler)
        layoutItemsOnCreate(recycler)
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
                /*if (i > totalVisibleItemCount) {
                    totalVisibleItemCount = i
                }*/
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
            if (Rect.intersects(displayRect, locationRects[i]) && !attachedItems[i]) {
                reuseItemOnSroll(i, false)
            } else {
                break
            }
        }

    }

    private fun reuseItemOnSroll(position: Int, addViewFromTop: Boolean) {
        recycler?.getViewForPosition(position)?.let { scrap ->
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
    }


    private fun layoutItem(child: View, rect: Rect, position: Int) {
        val layoutScrollTop = rect.top - scroll
        val layoutScrollBottom = rect.bottom - scroll
        val itemHeight: Int = rect.bottom - rect.top
        val totalItemCount = totalVisibleItemCount - stackCount
        val isPositionsNotEqualZero = position > 0 && totalItemCount > 0
        val layoutTopPositionArray = getLayoutTopPositionArray(itemHeight)
        val minLayoutTopPosition = layoutTopPositionArray.minOrNull()

        val layoutTop: Int
        val layoutBottom: Int

        if (isPositionsNotEqualZero && position >= totalItemCount && (minLayoutTopPosition != null && layoutScrollTop >= minLayoutTopPosition)) {
            val layoutTopNeed = getLayoutTopPosition(layoutScrollTop, layoutTopPositionArray)
            val heightRate =
                scaleLayoutItem(
                    child = child,
                    layoutTopNeedPair = layoutTopNeed,
                    layoutScrollTop = layoutScrollTop,
                    itemHeight = itemHeight
                )

            layoutTop = layoutScrollTop - heightRate
            layoutBottom = layoutScrollBottom - heightRate
        } else {
            resetLayoutItem(child)
            layoutTop = layoutScrollTop
            layoutBottom = layoutScrollBottom
        }
        layoutDecorated(child, rect.left, layoutTop, rect.right, layoutBottom)

    }

    private fun scaleLayoutItem(
        child: View,
        layoutTopNeedPair: Pair<Int, Int>?,
        layoutScrollTop: Int,
        itemHeight: Int
    ): Int {
        if (layoutTopNeedPair != null) {
            val layoutTopNeed = layoutTopNeedPair.second
            val positionInStackCount = abs(layoutTopNeedPair.first - stackCount)
            val rateInit = (layoutScrollTop - layoutTopNeed).toFloat() / itemHeight.toFloat()
            val scaleFactor = (1 - rateInit * rateInit / 3) - (positionInStackCount.toFloat() / 10f)
            val heightRate = (rateInit * itemHeight.toFloat()).toInt()
            child.scaleX = scaleFactor
            child.scaleY = scaleFactor
            child.z = -rateInit * (positionInStackCount + 1)
            //to solve appear item behind
            if (isAlpha || scaleFactor < 0.7f) {
                val alphaRate = 1 - rateInit * rateInit
                child.alpha = alphaRate
            }
            return heightRate + (positionInStackCount * heightRate)
        } else {
            resetLayoutItem(child)
            return 0
        }
    }

    private fun getLayoutTopPosition(
        layoutScrollTop: Int,
        layoutTopPositionArray: List<Int>
    ): Pair<Int, Int>? {
        layoutTopPositionArray.forEachIndexed { index, layoutTopPosition ->
            if (layoutScrollTop >= layoutTopPosition) {
                return Pair(index + 1, layoutTopPosition)
            }
        }
        return null
    }

    private fun getLayoutTopPositionArray(itemHeight: Int): List<Int> {
        val layoutTopPosition = mutableListOf<Int>()
        for (index in 1..stackCount) {
            val layoutTopNeed = (itemHeight * (totalVisibleItemCount - index)) - (itemHeight / 2)
            layoutTopPosition.add(layoutTopNeed)
        }
        return layoutTopPosition
    }

    private fun resetLayoutItem(child: View) {
        child.scaleX = 1f
        child.scaleY = 1f
        child.z = 0f
        child.alpha = 1f
    }


    fun changeRecyclerHeight(slideOffset: Float) {
        val scale = 1f - slideOffset
        val newHeight = (recyclerViewDefaultHeight * scale).toInt()
        changeRecyclerViewHeight(scale, newHeight)
    }

    private fun changeRecyclerViewHeight(scale: Float, newHeight: Int) {
        val params = recyclerView.layoutParams
        if (scale > 0.2f) params.height = newHeight
        recyclerView.layoutParams = params
    }

    companion object {
        const val TAG = "YOUSSEFF"
    }
}
