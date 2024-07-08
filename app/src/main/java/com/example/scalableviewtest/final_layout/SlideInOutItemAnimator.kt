package com.example.scalableviewtest.final_layout

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

class SlideInOutItemAnimator : SimpleItemAnimator() {
    private val duration = 300L

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        view.translationY = 0f
        view.alpha = 1f
        view.animate().translationY(-view.height.toFloat())//.alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dispatchAddFinished(holder)
                }
            }).start()
        return true
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        holder.itemView.translationY = -view.height.toFloat()
        holder.itemView.translationZ = -view.height.toFloat()
        view.animate().translationY(0f).translationZ(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dispatchRemoveFinished(holder)
                }
            }).start()
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
