package com.woodys.frame.transition

import android.animation.Animator
import android.view.View

/**
 * Created by woodys on 2017/7/24.
 */

interface FrameTransition {
    fun preAnim(v: View)

    fun transitionIn(v: View): Animator?

    fun transitionOut(v: View): Animator?
}