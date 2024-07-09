package com.example.scalableviewtest

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.scalableviewtest.adapter.ItemsAdapter
import com.example.scalableviewtest.databinding.XischeBottomsheetBinding
import com.example.scalableviewtest.final_layout.BottomStackLayoutManager
import com.example.scalableviewtest.final_layout.FadeInFadeOutItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior


class ScalableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lm: BottomStackLayoutManager? = null

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
                lm?.changeRecyclerHeight(slideOffset)
            }

        })

    }

    private fun setupBottomSheetInitialState(recyclerView: RecyclerView) = binding?.run {
        /* recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object :
             ViewTreeObserver.OnGlobalLayoutListener {
             override fun onGlobalLayout() {
                 ((recyclerView.rootView.height.toFloat() - recyclerView.layoutParams.height.toFloat()) / recyclerView.rootView.height.toFloat()).also { expandRatio ->
                      bottomSheetBehavior?.halfExpandedRatio =
                          if (expandRatio <= 0F || expandRatio >= 1) 0.1F else expandRatio
                  }
                  bottomSheetBehavior?.state = STATE_HALF_EXPANDED

                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

            }
        })*/
    }

    fun addContent(content: Int) = binding?.bottomSheet?.run {
        addView(LayoutInflater.from(context).inflate(content, null))
    }

    fun setAdapter(adapter: ItemsAdapter) = binding?.rv?.let {
        lm = BottomStackLayoutManager(it)
        it.adapter = adapter
        it.itemAnimator = FadeInFadeOutItemAnimator()
        it.layoutManager = lm
        setupBottomSheetInitialState(it)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }

}