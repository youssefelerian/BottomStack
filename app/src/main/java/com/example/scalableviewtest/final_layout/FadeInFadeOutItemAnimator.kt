package com.example.scalableviewtest.final_layout


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

class FadeInFadeOutItemAnimator : SimpleItemAnimator() {
    private val duration = 100L
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        fadeOut.setDuration(duration*holder.adapterPosition) // Duration for fade out
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                //view.alpha = 1f // Reset alpha
                dispatchRemoveFinished(holder)
            }
        })
        fadeOut.start()
        return true
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        view.alpha = 0f // Set initial alpha to 0
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        fadeIn.setDuration(duration*holder.adapterPosition) // Duration for fade in
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                dispatchAddFinished(holder)
            }
        })
        fadeIn.start()
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
