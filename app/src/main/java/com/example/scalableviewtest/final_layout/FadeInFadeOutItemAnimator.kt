package com.example.scalableviewtest.final_layout

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView


class FadeInFadeOutItemAnimator {
    fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val animator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 1f, 0f)
        animator.duration = 50
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                holder.itemView.alpha = 1f
            }
        })
        animator.start()
        return true
    }

    fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        val animator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f)
        animator.duration = 50
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }
        })
        animator.start()
        return true
    }

}
