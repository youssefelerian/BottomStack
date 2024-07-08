package com.example.scalableviewtest.final_layout

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

class ScaleInOutItemAnimator : SimpleItemAnimator() {

    private val duration = 250L

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f)
        scaleY.setDuration(duration) // Duration for scale out
        scaleY.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              //  view.scaleY = 1f // Reset scale
                dispatchRemoveFinished(holder)
            }
        })
        scaleY.start()
        return true
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        view.scaleY = 0f // Set initial scale to 0
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
        scaleY.setDuration(duration) // Duration for scale in
        scaleY.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                dispatchAddFinished(holder)
            }
        })
        scaleY.start()
        return true
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        dispatchMoveFinished(holder)
        return false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder,
        fromLeft: Int, fromTop: Int, toLeft: Int, toTop: Int
    ): Boolean {
        dispatchChangeFinished(oldHolder, true)
        dispatchChangeFinished(newHolder, false)
        return false
    }

    override fun runPendingAnimations() {
        // No pending animations
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        // End animations immediately
        item.itemView.clearAnimation()
    }

    override fun endAnimations() {
        // End all animations immediately
    }

    override fun isRunning(): Boolean {
        return false
    }
}
