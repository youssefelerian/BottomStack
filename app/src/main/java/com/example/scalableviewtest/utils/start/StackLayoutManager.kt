package com.example.scalableviewtest.utils.start

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import androidx.recyclerview.widget.RecyclerView.Recycler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.math.abs


class StackLayoutManager(private val recyclerView: RecyclerView) :
    LinearLayoutManager(recyclerView.context) {

    private var mSpace = 60
    private var mUnit = 0
    private var mItemWidth = 0
    private var mItemHeight = 0
    private var mTotalOffset = 0
    private var animator: ObjectAnimator? = null
    var animateValue: Int = 0
        set(value) {
            field = value
            val dy = field - lastAnimateValue
            fill(recycler, dy, false)
            lastAnimateValue = value
        }
    private val duration = 300
    private var recycler: Recycler? = null
    private var lastAnimateValue = 0
    private var maxStackCount = 4
    private var initialStackCount = 4
    private var secondaryScale = 0.8f
    private var scaleRatio = 0.4f
    private var parallex = 1f
    private var initialOffset = 0
    private var initial = false
    private var mMinVelocityX = 0
    private val mVelocityTracker: VelocityTracker = VelocityTracker.obtain()
    private var pointerId = 0
    private var sSetScrollState: Method? = null
    private var mPendingScrollPosition = DiffResult.NO_POSITION
    private var previousWidth = -1
    private var previousHeight = -1
    private var isInitialHeightSet = false
    private var enableResizeAnimation: Boolean = false
    private var totalHeight = 0

    constructor(recyclerView: RecyclerView, config: Config) : this(recyclerView) {
        Log.i(TAG, "config = $config")
        this.maxStackCount = config.maxStackCount
        this.mSpace = config.space
        this.initialStackCount = config.initialStackCount
        this.secondaryScale = config.secondaryScale
        this.scaleRatio = config.scaleRatio
        this.parallex = config.parallax
    }

    override fun isAutoMeasureEnabled(): Boolean {
        Log.i(TAG, "isAutoMeasureEnabled")
        return true
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        Log.i(TAG, "onLayoutChildren   state = $state")
        if (itemCount <= 0) return
        this.recycler = recycler

        try {
            val anchorView = recycler.getViewForPosition(0)
            measureChildWithMargins(anchorView, 0, 0)
            mItemWidth = anchorView.measuredWidth
            mItemHeight = anchorView.measuredHeight
            mUnit = if (canScrollHorizontally()) mItemWidth + mSpace else mItemHeight + mSpace
            setInitialHeight()

            initialOffset = resolveInitialOffset()
            mMinVelocityX = ViewConfiguration.get(anchorView.context).scaledMinimumFlingVelocity

            fill(recycler, 0)

            if ((initial || isRecyclerViewResized)) {
                simulateScrollOnResize()
                initial = true
            }
        } catch (ignored: Exception) {
        }

    }

    private val isRecyclerViewResized: Boolean
        get() {
            val newWidth = width
            val newHeight = height
            val resized = newWidth != previousWidth || newHeight != previousHeight
            previousWidth = newWidth
            previousHeight = newHeight
            return resized
        }

    private fun simulateScrollOnResize() {
        Log.i(TAG, "simulateScrollOnResize  ")
        val scrollDelta = calculateScrollDelta()
        if (scrollDelta != 0) {
            if (enableResizeAnimation) {
                if (canScrollHorizontally()) {
                    scrollHorizontallyBy(scrollDelta, recycler, null)
                } else {
                    scrollVerticallyBy(scrollDelta, recycler!!, null)
                }
            } else {
                enableResizeAnimation = true
                if (canScrollHorizontally()) {
                    scrollHorizontallyBy(totalHeight + mSpace, recycler, null)
                } else {
                    scrollVerticallyBy(
                        totalHeight + (maxStackCount * mSpace), recycler!!, null
                    )
                }
            }
        }
    }

    private fun calculateScrollDelta(): Int {
        Log.i(TAG, "calculateScrollDelta  ")
        val newWidth = width
        val newHeight = height
        if (previousWidth == -1 || previousHeight == -1) {
            previousWidth = newWidth
            previousHeight = newHeight
            return 0
        }
        val delta: Int = if (canScrollHorizontally()) {
            previousWidth - newWidth
        } else {
            previousHeight - newHeight
        }
        previousWidth = newWidth
        previousHeight = newHeight
        return delta
    }

    private fun resolveInitialOffset(): Int {
        Log.i(TAG, "resolveInitialOffset  ")
        var offset = initialStackCount * mUnit
        if (mPendingScrollPosition != DiffResult.NO_POSITION) {
            offset = mPendingScrollPosition * mUnit
            mPendingScrollPosition = DiffResult.NO_POSITION
        }
        return offset
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        super.onLayoutCompleted(state)
        Log.i(TAG, "onLayoutCompleted $state ")
        if (itemCount <= 0) return
        if (!initial) {
            fill(recycler, initialOffset, false)
            initial = true
        }
    }

    private fun setInitialHeight() {
        Log.i(TAG, "setInitialHeight ")
        if (isInitialHeightSet) return
        totalHeight = mItemHeight + (maxStackCount * mSpace) + mSpace
        val params = recyclerView.layoutParams
        params.height = totalHeight
        recyclerView.layoutParams = params
        isInitialHeightSet = true
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?, newAdapter: RecyclerView.Adapter<*>?
    ) {
        Log.i(TAG, "onAdapterChanged ")
        initial = false
        mTotalOffset = 0
    }

    private fun fill(recycler: Recycler?, dy: Int, apply: Boolean): Int {
        Log.i(TAG, "fill  $dy - apply = $apply ")
        var delta = dy
        if (apply) delta = (delta * parallex).toInt()
        return fillFromTop(recycler, -delta)
    }

    private fun fill(recycler: Recycler?, dy: Int): Int {
        Log.i(TAG, "fill  $dy ")
        return fill(recycler, dy, true)
    }

    private fun fillFromTop(recycler: Recycler?, dy: Int): Int {
        Log.i(TAG, "fillFromTop  $dy ")
        Log.w("YOUUUUUUU", "dy = $dy")
        if (mTotalOffset + dy < 0 || (mTotalOffset + dy + 0f) / mUnit > itemCount - 1) return 0
        detachAndScrapAttachedViews(recycler!!)
        mTotalOffset += dy
        Log.w("YOUUUUUUU", "mTotalOffset = $mTotalOffset")
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (recycleVertically(child, dy)) removeAndRecycleView(child!!, recycler)
        }
        Log.w("YOUUUUUUU", "count = $count")
        val currPos = mTotalOffset / mUnit
        val leavingSpace = height - (left(currPos) + mUnit)
        val itemCountAfterBaseItem = leavingSpace / mUnit + 2
        val e = currPos + itemCountAfterBaseItem
        val start = if (currPos - maxStackCount >= 0) currPos - maxStackCount else 0
        val end = if (e >= itemCount) itemCount - 1 else e
        val left = width / 2 - mItemWidth / 2
        Log.w("YOUUUUUUU", "currPos = $currPos - itemCountAfterBaseItem")
        for (i in start..end) {
            val view = recycler.getViewForPosition(i)
            Log.w("YOUUUUUUU", "getViewForPosition = $i")
            val scale = scale(i)
            // val alpha = alpha(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            val top = (left(i) - (1 - scale) * view.measuredHeight / 2).toInt()
            val right = view.measuredWidth + left
            val bottom = view.measuredHeight + top
            layoutDecoratedWithMargins(view, left, top, right, bottom)
            //view.alpha = alpha
            view.scaleY = scale
            view.scaleX = scale
        }
        return dy
    }

    private val mTouchListener = View.OnTouchListener { v, event ->
        Log.i(TAG, "mTouchListener  $event ")
        mVelocityTracker.addMovement(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (animator != null && animator!!.isRunning) animator!!.cancel()
            pointerId = event.getPointerId(0)
        }
        if (event.action == MotionEvent.ACTION_UP) {
            if (v.isPressed) v.performClick()
            mVelocityTracker.computeCurrentVelocity(1000, 14000f)
            val xVelocity = mVelocityTracker.getXVelocity(pointerId)
            val o = mTotalOffset % mUnit
            val scrollX: Int
            if (abs(xVelocity.toDouble()) < mMinVelocityX && o != 0) {
                scrollX = if (o >= mUnit / 2) mUnit - o
                else -o
                val dur = (abs(((scrollX + 0f) / mUnit).toDouble()) * duration).toInt()
                Log.i(TAG, "onTouch: ======BREW===")
                brewAndStartAnimator(dur, scrollX)
            }
        }
        false
    }

    private val mOnFlingListener: OnFlingListener = object : OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            Log.i(TAG, "OnFlingListener  onFling ")
            val o = mTotalOffset % mUnit
            val s = mUnit - o
            val scrollX: Int
            val vel = absMax(velocityX, velocityY)
            scrollX = if (vel > 0) {
                s
            } else -o
            val dur = computeSettleDuration(
                abs(scrollX.toDouble()).toInt(), abs(vel.toDouble()).toFloat()
            )
            brewAndStartAnimator(dur, scrollX)
            setScrollStateIdle()
            return true
        }
    }

    init {
        orientation = RecyclerView.VERTICAL
    }

    private fun absMax(a: Int, b: Int): Int {
        Log.i(TAG, "absMax ")
        return if (abs(a.toDouble()) > abs(b.toDouble())) a
        else b
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttachedToWindow(view: RecyclerView) {
        Log.i(TAG, "onAttachedToWindow ")
        super.onAttachedToWindow(view)
        view.setOnTouchListener(mTouchListener)
        view.onFlingListener = mOnFlingListener
    }

    private fun computeSettleDuration(distance: Int, xvel: Float): Int {
        Log.i(TAG, "computeSettleDuration ")
        val sWeight = 0.5f * distance / mUnit
        val velWeight = if (xvel > 0) 0.5f * mMinVelocityX / xvel else 0f
        return ((sWeight + velWeight) * duration).toInt()
    }

    private fun brewAndStartAnimator(dur: Int, finalXorY: Int) {
        animator =
            ObjectAnimator.ofInt(this@StackLayoutManager, "animateValue", 0, finalXorY).apply {
                duration = dur.toLong()
                start()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        lastAnimateValue = 0
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        lastAnimateValue = 0
                    }
                })
            }
    }

    /* private fun alpha(position: Int): Float {
         Log.i(TAG, "alpha alpha $position")
         val alpha: Float
         val currPos = mTotalOffset / mUnit
         val n = (mTotalOffset + .0f) / mUnit
         alpha = if (position > currPos) 1.0f
         else {
             1 - (n - position) / maxStackCount
         }
         return if (alpha <= 0.001f) 0f else alpha
     }*/

    private fun scale(position: Int): Float {
        val scale: Float
        val currPos = this.mTotalOffset / mUnit

        val n = (mTotalOffset + .0f) / mUnit
        val x = n - currPos
        scale = if (position >= currPos) {
            when (position) {
                currPos -> 1 - scaleRatio * (n - currPos) / maxStackCount
                currPos + 1 -> {
                    secondaryScale + (if (x > 0.5f) 1 - secondaryScale else 2 * (1 - secondaryScale) * x)
                }

                else -> secondaryScale
            }
        } else {
            if (position < currPos - maxStackCount) 0f
            else {
                1f - scaleRatio * (n - currPos + currPos - position) / maxStackCount
            }
        }
        Log.w(
            "YOUUUUUUUGGGGGG",
            "position = $position  - mTotalOffset = $mTotalOffset  -  mUnit= $mUnit   - currPos = $currPos   - scale = $scale"
        )
        return scale
    }

    private fun left(position: Int): Int {
        Log.i(TAG, "left left $position")
        val currPos = mTotalOffset / mUnit
        val tail = mTotalOffset % mUnit
        val n = (mTotalOffset + .0f) / mUnit
        val x = n - currPos
        return ltr(position, currPos, tail, x)
    }

    private fun ltr(position: Int, currPos: Int, tail: Int, x: Float): Int {
        var left: Int
        Log.i(TAG, "ltr ltr $position")
        if (position <= currPos) {
            left = if (position == currPos) {
                (mSpace * (maxStackCount - x)).toInt()
            } else {
                (mSpace * (maxStackCount - x - (currPos - position))).toInt()
            }
        } else {
            if (position == currPos + 1) left = mSpace * maxStackCount + mUnit - tail
            else {
                val closestBaseItemScale = scale(currPos + 1)
                val baseStart =
                    (mSpace * maxStackCount + mUnit - tail + closestBaseItemScale * (mUnit - mSpace) + mSpace).toInt()
                left =
                    (baseStart + (position - currPos - 2) * mUnit - (position - currPos - 2) * (1 - secondaryScale) * (mUnit - mSpace)).toInt()
            }
            left = if (left <= 0) 0 else left
        }
        return left
    }

    private fun recycleVertically(view: View?, dy: Int): Boolean {
        Log.i(TAG, "recycleVertically")
        return view != null && (view.top - dy < 0)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State?): Int {
        Log.i(TAG, "scrollVerticallyBy")
        return fill(recycler, -dy)
    }

    private fun setScrollStateIdle() {
        Log.i(TAG, "setScrollStateIdle")
        try {
            if (sSetScrollState == null) sSetScrollState =
                RecyclerView::class.java.getDeclaredMethod(
                    "setScrollState", Int::class.javaPrimitiveType
                )
            sSetScrollState!!.isAccessible = true
            sSetScrollState!!.invoke(recyclerView, RecyclerView.SCROLL_STATE_IDLE)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    override fun scrollToPosition(position: Int) {
        if (position > itemCount - 1) {
            return
        }
        val currPosition = mTotalOffset / mUnit
        val distance = (position - currPosition) * mUnit
        val dur = computeSettleDuration(abs(distance.toDouble()).toInt(), 0f)
        brewAndStartAnimator(dur, distance)
    }

    override fun requestLayout() {
        super.requestLayout()
        initial = false
    }

    companion object {
        private const val TAG = "StackLayoutYOUSSEF"
    }
}