package com.example.scalableviewtest

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.example.scalableviewtest.adapter.ItemsAdapter
import com.example.scalableviewtest.databinding.XischeBottomsheetBinding
import com.example.scalableviewtest.utils.Config
import com.example.scalableviewtest.utils.StackLayoutManager
import com.example.scalableviewtest.utils.YStackLayoutManager

class ScalableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var _topView: View? = null
    private var lm: LinearLayoutManager? = null

    private var binding: XischeBottomsheetBinding? =
        XischeBottomsheetBinding.inflate(LayoutInflater.from(context), this, true)


    private val bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? by lazy {
        binding?.bottomSheet?.let { BottomSheetBehavior.from(it) }
    }

    init {
        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val params = binding?.rv?.layoutParams
                params?.height =
                    (binding?.parent?.height ?: 0) - (bottomSheet.height * slideOffset).toInt()
                binding?.rv?.layoutParams = params
            }

        })

    }

    fun setAdapter(adapter: ItemsAdapter) = binding?.rv?.let {
        lm = StackLayoutManager(
            Config(
                space = 50,
                maxStackCount = 3,
                initialStackCount = 2,
                scaleRatio = 0.4f,
                secondaryScale = 1f,
                parallax = 2f
            ), it
        )
        it.adapter = adapter
        it.layoutManager = lm
        it.layoutParams.width = LayoutParams.MATCH_PARENT
        _topView = it
        setupBottomSheetInitialState(it)
    }

    private fun setupBottomSheetInitialState(recyclerView: RecyclerView) = binding?.run {
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                ((recyclerView.rootView.height.toFloat() - recyclerView.layoutParams.height.toFloat()) / recyclerView.rootView.height.toFloat()).also { expandRatio ->
                    bottomSheetBehavior?.halfExpandedRatio =
                        if (expandRatio <= 0F || expandRatio >= 1) 0.1F else expandRatio
                }
                bottomSheetBehavior?.state = STATE_HALF_EXPANDED
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

            }
        })
    }

    fun addContent(content: Int) = binding?.bottomSheet?.run {
        addView(LayoutInflater.from(context).inflate(content, null))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }

}