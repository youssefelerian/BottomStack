package com.example.scalableviewtest.final_layout

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class FadeInFadeOutItemAnimator {
    fun animateRemove(itemView: View, onAnimationEnd: () -> Unit): Boolean {
        val animator = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f)
        animator.duration = 50
        itemView.alpha = 1f
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                itemView.alpha = 0f
                onAnimationEnd.invoke()
            }
        })
        animator.start()
        return true
    }

    fun animateAdd(itemView: View, onAnimationEnd: () -> Unit): Boolean {
        itemView.alpha = 0f
        val animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
        animator.duration = 150
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                itemView.alpha = 1f
                onAnimationEnd.invoke()
            }
        })
        animator.start()
        return true
    }

}
