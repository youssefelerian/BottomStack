package com.example.scalableviewtest

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scalableviewtest.adapter.ItemsAdapter
import com.example.scalableviewtest.databinding.XischeBottomsheetBinding
import com.example.scalableviewtest.final_layout.BottomStackLayoutManager
import com.example.scalableviewtest.final_layout.BottomStackLayoutManager.Companion.TAG
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED


class ScalableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lm: LinearLayoutManager? = null

    private var binding: XischeBottomsheetBinding? =
        XischeBottomsheetBinding.inflate(LayoutInflater.from(context), this, true)

    private val recyclerViewTop = resources.getDimension(R.dimen.RecyclerViewTop).toInt()
    private val recyclerViewBottom = resources.getDimension(R.dimen.RecyclerViewBottom).toInt()

    private val bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? by lazy {
        binding?.bottomSheet?.let { BottomSheetBehavior.from(it) }
    }

    init {
        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding?.let {
                    val params = it.rv.layoutParams
                    params.height =
                        (it.parent.height) - (bottomSheet.height * slideOffset).toInt() - recyclerViewTop - recyclerViewBottom
                    it.rv.layoutParams = params
                }

            }

        })

    }

    /* private fun setupBottomSheetInitialState(recyclerView: RecyclerView) = binding?.run {
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
    }*/

    fun addContent(content: Int) = binding?.bottomSheet?.run {
        addView(LayoutInflater.from(context).inflate(content, null))
    }

    fun setAdapter(adapter: ItemsAdapter) = binding?.rv?.let {
        lm = BottomStackLayoutManager(it)
        it.adapter = adapter
        it.layoutManager = lm
        // setupBottomSheetInitialState(it)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }

}